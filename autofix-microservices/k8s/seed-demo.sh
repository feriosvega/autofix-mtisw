#!/usr/bin/env bash
# Carga datos de ejemplo a través del API Gateway (único punto de entrada al backend).
# Uso: ./seed-demo.sh               (usa http://$(minikube ip):30080)
#      ./seed-demo.sh http://IP:30080
set -euo pipefail
G="${1:-http://$(minikube ip):30080}"
J='Content-Type: application/json'
post() { curl -sf -H "$J" -d "$2" "$G$1" >/dev/null && echo "  ok  $1" || echo "  --  $1 (ya existía o fue rechazado)"; }
vid() { curl -sf "$G/api/v1/vehicles/plate/$1" | python3 -c "import json,sys; print(json.load(sys.stdin)['id'])"; }

echo "Vehículos (M1)"
post /api/v1/vehicles/ '{"plate":"BBCL21","brand":"Toyota","model":"Yaris","vehicleType":"SEDAN","manufactureYear":2015,"engineType":"GASOLINA","seats":5}'
post /api/v1/vehicles/ '{"plate":"HJKL34","brand":"Hyundai","model":"Tucson","vehicleType":"SUV","manufactureYear":2020,"engineType":"HIBRIDO","seats":5}'
post /api/v1/vehicles/ '{"plate":"PRST56","brand":"Ford","model":"Ranger","vehicleType":"PICKUP","manufactureYear":2012,"engineType":"DIESEL","seats":5}'
post /api/v1/vehicles/ '{"plate":"GHRT78","brand":"Kia","model":"Rio","vehicleType":"HATCHBACK","manufactureYear":2022,"engineType":"GASOLINA","seats":5}'
post /api/v1/vehicles/ '{"plate":"VWXY90","brand":"Peugeot","model":"Partner","vehicleType":"FURGONETA","manufactureYear":2008,"engineType":"ELECTRICO","seats":2}'

echo "Bonos TopCar de noviembre (M3)"
if curl -sf "$G/api/v1/bonuses/" | grep -q '"bonusYear":2026,"bonusMonth":11'; then echo "  --  bonos de 11/2026 ya cargados"; else
  post /api/v1/bonuses/email '{"text":"- Toyota: 5 bonos de 70.000 pesos\n- Ford: 2 bonos de 50.000 pesos\n- Hyundai: 1 bono de 30.000 pesos\n- Honda: 7 bonos de 40.000 pesos","year":2026,"month":11}'
fi

echo "Reparaciones (M3; consulta a M1 y M2 por RestTemplate + Eureka)"
rep() { # patente ingreso hora km bono salida retiro tipos...
  local id; id=$(vid "$1")
  if curl -sf "$G/api/v1/repairs/vehicle/$id" | grep -q "\"entryDate\":\"$2\""; then echo "  --  $1 $2 ya registrada"; return; fi
  local det=""; for t in "${@:8}"; do det="$det{\"repairType\":$t},"; done
  post /api/v1/repairs/ "{\"vehicleId\":$id,\"entryDate\":\"$2\",\"entryTime\":\"$3\",\"mileage\":$4,\"applyBonus\":$5,\"exitDate\":\"$6\",\"exitTime\":\"17:00\",\"pickupDate\":\"$7\",\"pickupTime\":\"18:00\",\"details\":[${det%,}]}"
}
rep BBCL21 2026-09-07 09:30 30000 false 2026-09-08 2026-09-08 1 7
rep HJKL34 2026-09-15 15:00  8000 false 2026-09-17 2026-09-19 2
rep PRST56 2026-10-01 10:00 45000 false 2026-10-03 2026-10-03 1 3 8
rep GHRT78 2026-10-16 11:00  4000 false 2026-10-16 2026-10-17 7
rep VWXY90 2026-10-20 14:00 60000 false 2026-10-22 2026-10-22 5 11
rep BBCL21 2026-11-02 10:30 33000 true  2026-11-04 2026-11-06 3
rep PRST56 2026-11-05 09:15 47000 true  2026-11-06 2026-11-06 1 4
rep HJKL34 2026-11-10 16:00  9500 true  2026-11-12 2026-11-12 1 9
echo "Listo. Abre el frontend en ${G%:30080}:30081"
