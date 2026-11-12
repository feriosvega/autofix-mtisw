import { useState } from "react";
import reportService from "../services/report.service";
import { errorMessage, money } from "../utils/format";
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

export default function ReportR2() {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const search = () => reportService.get("r2", month, year).then((r) => setData(r.data)).catch((e) => setError(errorMessage(e)));
  return <Paper sx={{ p: 2 }}>
    <Typography variant="h5">R2: Comparativo de reparaciones</Typography>
    <Typography variant="body2" color="text.secondary">Cantidad y monto de cada reparación en el mes seleccionado y los dos meses anteriores.</Typography>
    <Stack direction="row" spacing={2} sx={{ my: 2 }}>
      <TextField label="Mes de referencia" type="number" inputProps={{ min: 1, max: 12 }} value={month} onChange={(e) => setMonth(Number(e.target.value))} />
      <TextField label="Año" type="number" inputProps={{ min: 2000, max: 2100 }} value={year} onChange={(e) => setYear(Number(e.target.value))} />
      <Button variant="contained" startIcon={<SearchIcon />} onClick={search}>Generar</Button>
    </Stack>
    {error && <Alert severity="error">{error}</Alert>}
    {data && <Table size="small"><TableHead><TableRow>
      <TableCell>Tipo de reparación</TableCell>{data.months.map((m) => <TableCell key={m.label} align="right">{m.label}<br />cantidad · monto · variación</TableCell>)}
    </TableRow></TableHead><TableBody>
      {data.rows.map((row) => <TableRow key={row.repairTypeId}>
        <TableCell>{row.repairTypeId}. {row.repairName}</TableCell>
        {row.months.map((m, index) => <TableCell key={m.label} align="right">
          <div>{m.quantity} · {money(m.amount)}</div>
          {index > 0 && <small>Δ {m.deltaQuantity} ({m.quantityVariationPercent == null ? "N/A" : m.quantityVariationPercent.toFixed(1) + "%"}) · {money(m.deltaAmount)} ({m.amountVariationPercent == null ? "N/A" : m.amountVariationPercent.toFixed(1) + "%"})</small>}
        </TableCell>)}
      </TableRow>)}
      <TableRow sx={{ backgroundColor: "#e3f2fd" }}><TableCell sx={{ fontWeight: "bold" }}>TOTAL</TableCell>
        {data.months.map((month) => {
          const quantity = data.rows.reduce((sum, row) => sum + row.months.find((m) => m.label === month.label).quantity, 0);
          const amount = data.rows.reduce((sum, row) => sum + row.months.find((m) => m.label === month.label).amount, 0);
          return <TableCell key={month.label} align="right" sx={{ fontWeight: "bold" }}>{quantity} · {money(amount)}</TableCell>;
        })}
      </TableRow>
    </TableBody></Table>}
  </Paper>;
}
