import { useState } from "react";
import reportService from "../services/report.service";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
import DateRangeForm from "./DateRangeForm";
import { LABELS } from "../utils/constants";
import { errorMessage, money } from "../utils/format";

const TITLES = {
  r2: "R2: Reparaciones vs tipo de vehículo",
  r4: "R4: Reparaciones vs tipo de motor",
};

// Cada celda muestra N° de vehículos reparados y monto total
const Cell = ({ cell, bold }) => (
  <TableCell align="center" sx={{ fontWeight: bold ? "bold" : "normal" }}>
    <div>{cell.count} veh.</div>
    <div style={{ color: "#555" }}>{money(cell.amount)}</div>
  </TableCell>
);

// HU5 (R2) y HU7 (R4): misma estructura de matriz, distinta columna (tipo de vehículo / tipo de motor)
const MatrixReport = ({ report }) => {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  const search = (from, to) => {
    setError("");
    reportService.get(report, from, to).then((r) => setData(r.data)).catch((e) => setError(errorMessage(e)));
  };

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="h5">{TITLES[report]}</Typography>
      <Typography variant="body2" color="text.secondary">Ordenado de mayor a menor monto total</Typography>
      <DateRangeForm onSearch={search} />
      {error && <Alert severity="error">{error}</Alert>}
      {data && (
        <TableContainer>
          <Table size="small" sx={{ "& td, & th": { whiteSpace: "nowrap" } }}>
            <TableHead>
              <TableRow>
                <TableCell sx={{ fontWeight: "bold" }}>Reparación</TableCell>
                {data.columns.map((c) => <TableCell key={c} align="center" sx={{ fontWeight: "bold" }}>{LABELS[c]}</TableCell>)}
                <TableCell align="center" sx={{ fontWeight: "bold", backgroundColor: "#e3f2fd" }}>TOTAL</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {data.rows.map((row) => (
                <TableRow key={row.repairType}>
                  <TableCell>{row.repairType}. {row.repairName}</TableCell>
                  {data.columns.map((c) => <Cell key={c} cell={row.cells[c]} />)}
                  <Cell cell={row.total} bold />
                </TableRow>
              ))}
              <TableRow sx={{ backgroundColor: "#e3f2fd" }}>
                <TableCell sx={{ fontWeight: "bold" }}>TOTAL</TableCell>
                {data.columns.map((c) => <Cell key={c} cell={data.columnTotals[c]} bold />)}
                <Cell cell={data.grandTotal} bold />
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Paper>
  );
};

export default MatrixReport;
