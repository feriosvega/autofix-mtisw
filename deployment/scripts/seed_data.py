#!/usr/bin/env python3
"""Carga datos de ejemplo en AutoFix usando la API REST (útil para la demo de reportes).

Uso:  python3 seed_data.py http://<PUBLIC_HOST>      (por defecto http://localhost)
Obtiene un token del usuario admin en Keycloak (grant password) y registra vehículos, bonos y reparaciones.
"""
import json
import sys
import urllib.parse
import urllib.request
from datetime import date, timedelta

BASE = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost").rstrip("/")
USER, PASSWORD = "admin", "admin123"


def token():
    data = urllib.parse.urlencode({"grant_type": "password", "client_id": "autofix-frontend",
                                   "username": USER, "password": PASSWORD}).encode()
    req = urllib.request.Request(f"{BASE}/auth/realms/autofix-realm/protocol/openid-connect/token", data=data)
    return json.load(urllib.request.urlopen(req))["access_token"]


TOKEN = token()


def call(method, path, body=None):
    req = urllib.request.Request(f"{BASE}{path}", method=method,
                                 data=json.dumps(body).encode() if body is not None else None,
                                 headers={"Authorization": f"Bearer {TOKEN}", "Content-Type": "application/json"})
    try:
        raw = urllib.request.urlopen(req).read()
        return json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        print("  !", method, path, e.code, e.read().decode()[:200])
        return None


today = date.today()
month_start = today.replace(day=1)

vehicles = [
    ("ABCD12", "Toyota", "Corolla", "SEDAN", 2015, "GASOLINA", 5),
    ("BCDF34", "Kia", "Rio 5", "HATCHBACK", 2020, "GASOLINA", 5),
    ("CDFG56", "Hyundai", "Tucson", "SUV", 2012, "DIESEL", 5),
    ("DFGH78", "Ford", "Ranger", "PICKUP", 2008, "DIESEL", 5),
    ("FGHJ90", "Honda", "CR-V Hybrid", "SUV", 2022, "HIBRIDO", 5),
    ("GHJK11", "Chevrolet", "N400", "FURGONETA", 2018, "GASOLINA", 8),
    ("HJKL22", "BYD", "Dolphin", "HATCHBACK", 2024, "ELECTRICO", 5),
    ("JKLM33", "Toyota", "Hilux", "PICKUP", 2019, "DIESEL", 5),
]
ids = {}
existing = {v["plate"]: v["id"] for v in (call("GET", "/api/v1/vehicles/") or [])}
for plate, brand, model, vtype, year, engine, seats in vehicles:
    if plate in existing:
        ids[plate] = existing[plate]
        continue
    v = call("POST", "/api/v1/vehicles/", {"plate": plate, "brand": brand, "model": model, "vehicleType": vtype,
                                            "manufactureYear": year, "engineType": engine, "seats": seats})
    if v:
        ids[plate] = v["id"]
print("Vehículos:", len(ids))

call("POST", "/api/v1/bonuses/email", {"year": today.year, "month": today.month,
                                       "text": "- Toyota: 5 bonos de 70.000 pesos\n- Ford: 2 bonos de 50.000 pesos\n"
                                               "- Hyundai: 1 bono de 30.000 pesos\n- Honda: 7 bonos de 40.000 pesos"})
print("Bonos del mes cargados")

# (patente, días desde inicio de mes, hora ingreso, km, reparaciones, horas en taller, días de retraso, bono)
repairs = [
    ("ABCD12", 0, "10:00", 15000, [1, 5], 50, 2, True),
    ("BCDF34", 1, "09:30", 8000, [7, 11], 6, 0, False),
    ("CDFG56", 2, "14:00", 60000, [3, 4], 96, 1, True),
    ("DFGH78", 3, "11:00", 130000, [1, 8, 6], 30, 0, True),
    ("FGHJ90", 4, "16:00", 20000, [9], 8, 3, False),
    ("GHJK11", 5, "08:00", 35000, [2, 10], 26, 0, False),
    ("HJKL22", 6, "10:30", 4000, [5, 7], 5, 0, False),
    ("JKLM33", 7, "12:00", 45000, [3], 72, 0, True),
    ("ABCD12", 8, "09:00", 16000, [11], 3, 0, False),
]
if call("GET", "/api/v1/repairs/"):
    print("Ya existen reparaciones: no se cargan de nuevo (el script es idempotente)")
    repairs = []
for plate, day, hour, km, types, hours, late, bonus in repairs:
    if plate not in ids:
        continue
    entry = month_start + timedelta(days=day)
    h, m = map(int, hour.split(":"))
    exit_total = h + hours
    exit_date = entry + timedelta(days=exit_total // 24)
    exit_time = f"{exit_total % 24:02d}:{m:02d}"
    pickup = exit_date + timedelta(days=late)
    call("POST", "/api/v1/repairs/", {
        "vehicleId": ids[plate], "entryDate": entry.isoformat(), "entryTime": hour, "mileage": km,
        "exitDate": exit_date.isoformat(), "exitTime": exit_time,
        "pickupDate": pickup.isoformat(), "pickupTime": "18:00" if late else max(exit_time, "18:00"),
        "applyBonus": bonus, "details": [{"repairType": t} for t in types]})
print("Reparaciones registradas. Revise los reportes desde", month_start, "hasta", month_start + timedelta(days=20))
