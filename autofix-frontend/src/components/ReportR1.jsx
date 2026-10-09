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

const MONEY_COLUMNS = [
  ["repairsAmount", "Suma reparaciones"], ["mileageSurcharge", "Rec. km"], ["ageSurcharge", "Rec. antigüedad"],
  ["lateSurcharge", "Rec. retraso"], ["repairsCountDiscount", "Desc. N° rep."], ["dayDiscount", "Desc. día"],
  ["bonusDiscount", "Desc. bono"], ["subtotal", "Subtotal"], ["iva", "IVA"], ["totalAmount", "Total"],
];

// HU4 - R1: vehículos del rango con todos los valores de la fórmula y totales al final
const ReportR1 = () => {
  const [report, setReport] = useState(null);
  const [error, setError] = useState("");

  const search = (from, to) => {
    setError("");
    reportService.get("r1", from, to).then((r) => setReport(r.data)).catch((e) => setError(errorMessage(e)));
  };

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="h5">R1: Detalle del costo de reparación por vehículo</Typography>
      <DateRangeForm onSearch={search} />
      {error && <Alert severity="error">{error}</Alert>}
      {report && (
        <TableContainer>
          <Table size="small" sx={{ "& td, & th": { whiteSpace: "nowrap" } }}>
            <TableHead>
              <TableRow>
                {["Patente", "Marca", "Modelo", "Tipo", "Motor", "Año", "Km", "Ingreso", "N° rep."].map((h) => (
                  <TableCell key={h} sx={{ fontWeight: "bold" }}>{h}</TableCell>
                ))}
                {MONEY_COLUMNS.map(([, h]) => <TableCell key={h} align="right" sx={{ fontWeight: "bold" }}>{h}</TableCell>)}
              </TableRow>
            </TableHead>
            <TableBody>
              {report.rows.map((r) => (
                <TableRow key={r.repairId}>
                  <TableCell>{r.plate}</TableCell>
                  <TableCell>{r.brand}</TableCell>
                  <TableCell>{r.model}</TableCell>
                  <TableCell>{LABELS[r.vehicleType]}</TableCell>
                  <TableCell>{LABELS[r.engineType]}</TableCell>
                  <TableCell>{r.manufactureYear}</TableCell>
                  <TableCell>{r.mileage.toLocaleString("es-CL")}</TableCell>
                  <TableCell>{r.entryDate}</TableCell>
                  <TableCell>{r.repairsCount}</TableCell>
                  {MONEY_COLUMNS.map(([k]) => <TableCell key={k} align="right">{money(r[k])}</TableCell>)}
                </TableRow>
              ))}
              <TableRow sx={{ backgroundColor: "#e3f2fd" }}>
                <TableCell colSpan={8} sx={{ fontWeight: "bold" }}>TOTAL ({report.rows.length} vehículos)</TableCell>
                <TableCell sx={{ fontWeight: "bold" }}>{report.totals.repairsCount}</TableCell>
                {MONEY_COLUMNS.map(([k]) => <TableCell key={k} align="right" sx={{ fontWeight: "bold" }}>{money(report.totals[k])}</TableCell>)}
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      )}
    </Paper>
  );
};

export default ReportR1;
