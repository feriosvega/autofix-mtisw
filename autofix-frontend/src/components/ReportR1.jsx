import { useState } from "react";
import reportService from "../services/report.service";
import { errorMessage, money } from "../utils/format";
import { LABELS } from "../utils/constants";
import Paper from "@mui/material/Paper";
import Typography from "@mui/material/Typography";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Alert from "@mui/material/Alert";
import Table from "@mui/material/Table";
import TableHead from "@mui/material/TableHead";
import TableBody from "@mui/material/TableBody";
import TableRow from "@mui/material/TableRow";
import TableCell from "@mui/material/TableCell";
import SearchIcon from "@mui/icons-material/Search";

const VEHICLE_TYPES = ["SEDAN", "HATCHBACK", "SUV", "PICKUP", "FURGONETA"];
export default function ReportR1() {
  const today = new Date();
  const [month, setMonth] = useState(today.getMonth() + 1);
  const [year, setYear] = useState(today.getFullYear());
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const search = () => reportService.get("r1", month, year).then((r) => setData(r.data)).catch((e) => setError(errorMessage(e)));
  return <Paper sx={{ p: 2 }}>
    <Typography variant="h5">R1: Reparaciones por tipo de vehículo</Typography>
    <Typography variant="body2" color="text.secondary">Cantidad y monto total por tipo de reparación para el mes y año seleccionados.</Typography>
    <Stack direction="row" spacing={2} sx={{ my: 2 }}>
      <TextField label="Mes" type="number" inputProps={{ min: 1, max: 12 }} value={month} onChange={(e) => setMonth(Number(e.target.value))} />
      <TextField label="Año" type="number" inputProps={{ min: 2000, max: 2100 }} value={year} onChange={(e) => setYear(Number(e.target.value))} />
      <Button variant="contained" startIcon={<SearchIcon />} onClick={search}>Generar</Button>
    </Stack>
    {error && <Alert severity="error">{error}</Alert>}
    {data && <Table size="small"><TableHead><TableRow>
      <TableCell>Tipo de reparación</TableCell>{VEHICLE_TYPES.map((type) => <TableCell key={type} align="right">{LABELS[type]} · cantidad</TableCell>)}
      {VEHICLE_TYPES.map((type) => <TableCell key={`amount-${type}`} align="right">{LABELS[type]} · monto</TableCell>)}
      <TableCell align="right">Total</TableCell>
    </TableRow></TableHead><TableBody>
      {data.rows.map((row) => <TableRow key={row.repairTypeId}>
        <TableCell>{row.repairTypeId}. {row.repairName}</TableCell>
        {VEHICLE_TYPES.map((type) => <TableCell key={`count-${type}`} align="right">{row.countsByVehicleType[type]}</TableCell>)}
        {VEHICLE_TYPES.map((type) => <TableCell key={`sum-${type}`} align="right">{money(row.amountByVehicleType[type])}</TableCell>)}
        <TableCell align="right">{row.totalCount} · {money(row.totalAmount)}</TableCell>
      </TableRow>)}
      <TableRow sx={{ backgroundColor: "#e3f2fd" }}><TableCell sx={{ fontWeight: "bold" }}>TOTAL</TableCell>
        {VEHICLE_TYPES.map((type) => <TableCell key={`total-count-${type}`} align="right">{data.totalCountsByVehicleType[type]}</TableCell>)}
        {VEHICLE_TYPES.map((type) => <TableCell key={`total-amount-${type}`} align="right">{money(data.totalAmountByVehicleType[type])}</TableCell>)}
        <TableCell align="right">{data.totalCount} · {money(data.totalAmount)}</TableCell>
      </TableRow>
    </TableBody></Table>}
  </Paper>;
}
