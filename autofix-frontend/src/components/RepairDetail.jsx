import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import repairService from "../services/repair.service";
import vehicleService from "../services/vehicle.service";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
import Grid from "@mui/material/Grid";
import { LABELS, REPAIR_NAMES } from "../utils/constants";
import { errorMessage, money, time } from "../utils/format";

// HU3: desglose del costo total = [Suma(Reparaciones) + Recargos - Descuentos] + IVA
const RepairDetail = () => {
  const { id } = useParams();
  const [repair, setRepair] = useState(null);
  const [vehicle, setVehicle] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    repairService
      .get(id)
      .then((r) => {
        setRepair(r.data);
        return vehicleService.get(r.data.vehicleId);
      })
      .then((v) => setVehicle(v.data))
      .catch((e) => setError(errorMessage(e)));
  }, [id]);

  if (error) return <Alert severity="error">{error}</Alert>;
  if (!repair || !vehicle) return <div>Cargando...</div>;

  const row = (label, value, bold, color) => (
    <TableRow>
      <TableCell sx={{ fontWeight: bold ? "bold" : "normal" }}>{label}</TableCell>
      <TableCell align="right" sx={{ fontWeight: bold ? "bold" : "normal", color }}>{value}</TableCell>
    </TableRow>
  );

  return (
    <Grid container spacing={2}>
      <Grid item xs={12} md={5}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6">Vehículo {vehicle.plate}</Typography>
          <Typography>{vehicle.brand} {vehicle.model} · {LABELS[vehicle.vehicleType]} · {LABELS[vehicle.engineType]}</Typography>
          <Typography>Año {vehicle.manufactureYear} · {vehicle.seats} asientos · {repair.mileage.toLocaleString("es-CL")} km</Typography>
          <Typography sx={{ mt: 2 }} variant="subtitle2">Historial</Typography>
          <Typography>Ingreso: {repair.entryDate} {time(repair.entryTime)}</Typography>
          <Typography>Salida reparación: {repair.exitDate ? `${repair.exitDate} ${time(repair.exitTime)}` : "en taller"}</Typography>
          <Typography>Retiro cliente: {repair.pickupDate ? `${repair.pickupDate} ${time(repair.pickupTime)}` : "-"}</Typography>
          <Typography sx={{ mt: 2 }} variant="subtitle2">Reparaciones</Typography>
          <Table size="small">
            <TableBody>
              {repair.details.map((d) => (
                <TableRow key={d.id}>
                  <TableCell>{d.repairType}. {REPAIR_NAMES[d.repairType]}</TableCell>
                  <TableCell align="right">{money(d.price)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      </Grid>
      <Grid item xs={12} md={7}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6">Cálculo del costo total (HU3)</Typography>
          <Table size="small">
            <TableBody>
              {row("Suma de reparaciones", money(repair.repairsAmount), true)}
              {row("+ Recargo por kilometraje", money(repair.mileageSurcharge), false, "#c62828")}
              {row("+ Recargo por antigüedad", money(repair.ageSurcharge), false, "#c62828")}
              {row("+ Recargo por retraso en la recogida", money(repair.lateSurcharge), false, "#c62828")}
              {row("- Descuento por N° de reparaciones (12 meses)", money(repair.repairsCountDiscount), false, "#2e7d32")}
              {row("- Descuento por día de atención", money(repair.dayDiscount), false, "#2e7d32")}
              {row("- Descuento por bono", money(repair.bonusDiscount), false, "#2e7d32")}
              {row("Subtotal", money(repair.subtotal), true)}
              {row("+ IVA (19%)", money(repair.iva))}
              {row("COSTO TOTAL", money(repair.totalAmount), true)}
            </TableBody>
          </Table>
        </Paper>
        <div style={{ marginTop: 12 }}><Link to="/repairs/list">Volver al historial</Link></div>
      </Grid>
    </Grid>
  );
};

export default RepairDetail;
