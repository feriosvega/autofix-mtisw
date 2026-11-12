import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import repairService from "../services/repair.service";
import { errorMessage, money, time } from "../utils/format";
import { LABELS } from "../utils/constants";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import TableContainer from "@mui/material/TableContainer";
import Table from "@mui/material/Table";
import TableHead from "@mui/material/TableHead";
import TableBody from "@mui/material/TableBody";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import Alert from "@mui/material/Alert";

// HU4: listado de TODOS los vehículos con TODOS los campos del historial de reparaciones,
// con la misma estructura de columnas del enunciado. Una fila por ingreso al taller.
const surcharges = (r) => r.mileageSurcharge + r.ageSurcharge + r.lateSurcharge;
const discounts = (r) => r.repairsCountDiscount + r.dayDiscount + r.bonusDiscount;
const amount = (r, value) => (r.pickupDate ? money(value) : "Al retiro");

const COLUMNS = [
  ["Patente Vehículo", (v) => v.plate],
  ["Marca Vehículo", (v) => v.brand],
  ["Modelo Vehículo", (v) => v.model],
  ["Tipo Vehículo", (v) => LABELS[v.vehicleType]],
  ["Año Fabricación", (v) => v.manufactureYear],
  ["Tipo Motor", (v) => LABELS[v.engineType]],
  ["Fecha Ingreso Taller", (v, r) => r?.entryDate],
  ["Hora Ingreso Taller", (v, r) => time(r?.entryTime)],
  ["Monto Total Reparaciones", (v, r) => r && money(r.repairsAmount), true],
  ["Monto Recargos", (v, r) => r && amount(r, surcharges(r)), true],
  ["Monto Dctos", (v, r) => r && amount(r, discounts(r)), true],
  ["SUB Total", (v, r) => r && amount(r, r.subtotal), true],
  ["Monto IVA", (v, r) => r && amount(r, r.iva), true],
  ["Costo Total", (v, r) => r && amount(r, r.totalAmount), true],
  ["Fecha Salida Taller", (v, r) => r?.exitDate],
  ["Hora Salida Taller", (v, r) => time(r?.exitTime)],
  ["Fecha Retiro Cliente", (v, r) => r?.pickupDate],
  ["Hora Retiro Cliente", (v, r) => time(r?.pickupTime)],
];

export default function RepairHistory() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  useEffect(() => { repairService.getHistory().then((r) => setRows(r.data)).catch((e) => setError(errorMessage(e))); }, []);

  // Vehículos sin reparaciones también aparecen (con los campos del historial vacíos)
  const lines = rows.flatMap(({ vehicle, repairs }) =>
    repairs.length === 0 ? [{ vehicle, repair: null }] : repairs.map((repair) => ({ vehicle, repair })));

  return <Paper sx={{ p: 2 }}>
    <Typography variant="h5" gutterBottom>Historial de reparaciones de todos los vehículos (HU4)</Typography>
    <Typography variant="body2" color="text.secondary" gutterBottom>
      Los montos de recargos, descuentos, IVA y costo total se calculan cuando el cliente retira el vehículo. Clic en una fila para ver el detalle.
    </Typography>
    {error && <Alert severity="error">{error}</Alert>}
    <TableContainer>
      <Table size="small">
        <TableHead><TableRow>
          {COLUMNS.map(([header, , right]) => <TableCell key={header} align={right ? "right" : "left"} sx={{ fontWeight: "bold" }}>{header}</TableCell>)}
        </TableRow></TableHead>
        <TableBody>
          {lines.map(({ vehicle, repair }) => (
            <TableRow key={vehicle.id + "-" + (repair?.id ?? "none")} hover sx={{ cursor: repair ? "pointer" : "default" }}
              onClick={() => repair && navigate("/repairs/" + repair.id)}>
              {COLUMNS.map(([header, value, right]) => <TableCell key={header} align={right ? "right" : "left"} sx={{ whiteSpace: "nowrap" }}>{value(vehicle, repair) ?? ""}</TableCell>)}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  </Paper>;
}
