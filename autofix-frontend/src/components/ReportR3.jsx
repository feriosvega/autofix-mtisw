import { useState } from "react";
import reportService from "../services/report.service";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
import DateRangeForm from "./DateRangeForm";
import { LABELS } from "../utils/constants";
import { errorMessage } from "../utils/format";

const hours = (h) => `${h.toLocaleString("es-CL")} h`;

// HU6 - R3: tiempo de reparación (ingreso -> salida de la reparación) por tipo de vehículo
const ReportR3 = () => {
  const [rows, setRows] = useState(null);
  const [error, setError] = useState("");

  const search = (from, to) => {
    setError("");
    reportService.get("r3", from, to).then((r) => setRows(r.data)).catch((e) => setError(errorMessage(e)));
  };

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="h5">R3: Tiempos de reparación por tipo de vehículo</Typography>
      <Typography variant="body2" color="text.secondary">Tiempo = salida de la reparación − ingreso al taller (en horas)</Typography>
      <DateRangeForm onSearch={search} />
      {error && <Alert severity="error">{error}</Alert>}
      {rows && (
        <Table size="small">
          <TableHead>
            <TableRow>
              {["Tipo de vehículo", "N° reparaciones", "Promedio", "Desv. estándar", "Mínimo", "Máximo", "Percentil 90"].map((h) => (
                <TableCell key={h} sx={{ fontWeight: "bold" }}>{h}</TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((r) => (
              <TableRow key={r.vehicleType}>
                <TableCell>{LABELS[r.vehicleType]}</TableCell>
                <TableCell>{r.repairsCount}</TableCell>
                <TableCell>{hours(r.averageHours)}</TableCell>
                <TableCell>{hours(r.standardDeviationHours)}</TableCell>
                <TableCell>{hours(r.minHours)}</TableCell>
                <TableCell>{hours(r.maxHours)}</TableCell>
                <TableCell>{hours(r.percentile90Hours)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </Paper>
  );
};

export default ReportR3;
