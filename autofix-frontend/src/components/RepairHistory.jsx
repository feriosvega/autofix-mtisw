import { useEffect, useState } from "react";
import repairService from "../services/repair.service";
import { errorMessage, money, time } from "../utils/format";
import { LABELS } from "../utils/constants";
import Paper from "@mui/material/Paper";
import Accordion from "@mui/material/Accordion";
import AccordionSummary from "@mui/material/AccordionSummary";
import AccordionDetails from "@mui/material/AccordionDetails";
import Typography from "@mui/material/Typography";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import Table from "@mui/material/Table";
import TableHead from "@mui/material/TableHead";
import TableBody from "@mui/material/TableBody";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import Alert from "@mui/material/Alert";
import Grid from "@mui/material/Grid";

const COSTS = [
  ["repairsAmount", "Suma de reparaciones"], ["mileageSurcharge", "Recargo por kilometraje"],
  ["ageSurcharge", "Recargo por antigüedad"], ["lateSurcharge", "Recargo por retraso"],
  ["repairsCountDiscount", "Descuento por reparaciones previas"], ["dayDiscount", "Descuento por día de atención"], ["bonusDiscount", "Descuento por bono"],
  ["iva", "IVA"], ["totalAmount", "Costo total"],
];

export default function RepairHistory() {
  const [rows, setRows] = useState([]);
  const [error, setError] = useState("");
  useEffect(() => { repairService.getHistory().then((r) => setRows(r.data)).catch((e) => setError(errorMessage(e))); }, []);
  return <Paper sx={{ p: 2 }}>
    <Typography variant="h5" gutterBottom>Historial completo de vehículos (HU4)</Typography>
    {error && <Alert severity="error">{error}</Alert>}
    {rows.map(({ vehicle, repairs }) => <Accordion key={vehicle.id}>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Typography><b>{vehicle.plate}</b> · {vehicle.brand} {vehicle.model} · {LABELS[vehicle.vehicleType]} · {LABELS[vehicle.engineType]} · Año {vehicle.manufactureYear} · {vehicle.seats} asientos</Typography>
      </AccordionSummary>
      <AccordionDetails>
        {repairs.length === 0 ? <Typography color="text.secondary">Sin reparaciones registradas.</Typography> : repairs.map((repair) => <Paper key={repair.id} variant="outlined" sx={{ p: 1.5, mb: 1.5 }}>
          <Typography variant="subtitle1">Ingreso: {repair.entryDate} {time(repair.entryTime)} · Kilometraje: {Number(repair.mileage).toLocaleString("es-CL")} km</Typography>
          <Typography variant="body2">Salida listo: {repair.exitDate ? repair.exitDate + " " + time(repair.exitTime) : "Pendiente"} · Retiro: {repair.pickupDate ? repair.pickupDate + " " + time(repair.pickupTime) : "Pendiente"}</Typography>
          <Table size="small"><TableHead><TableRow>{["Tipo de reparación", "Fecha", "Hora", "Monto"].map((h) => <TableCell key={h}>{h}</TableCell>)}</TableRow></TableHead>
            <TableBody>{repair.details.map((detail) => <TableRow key={detail.id}>
              <TableCell>{detail.repairType}. {detail.repairName}</TableCell><TableCell>{detail.repairDate}</TableCell><TableCell>{time(detail.repairTime)}</TableCell><TableCell>{money(detail.price)}</TableCell>
            </TableRow>)}</TableBody>
          </Table>
          <Grid container spacing={1} sx={{ mt: 1 }}>
            {COSTS.map(([field, label]) => <Grid item xs={6} md={3} key={field}>
              <Typography variant="caption" display="block" color="text.secondary">{label}</Typography>
              <Typography fontWeight={field === "totalAmount" ? "bold" : "normal"}>{money(repair[field])}</Typography>
            </Grid>)}
          </Grid>
        </Paper>)}
      </AccordionDetails>
    </Accordion>)}
  </Paper>;
}
