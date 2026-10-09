import { useState } from "react";
import Stack from "@mui/material/Stack";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import SearchIcon from "@mui/icons-material/Search";
import { firstDayOfMonth, today } from "../utils/format";

// Rango de fechas ingresado por el usuario (común a R1..R4)
export default function DateRangeForm({ onSearch }) {
  const [from, setFrom] = useState(firstDayOfMonth());
  const [to, setTo] = useState(today());

  return (
    <Stack direction="row" spacing={2} sx={{ my: 2 }}>
      <TextField type="date" label="Desde" InputLabelProps={{ shrink: true }} value={from} onChange={(e) => setFrom(e.target.value)} size="small" />
      <TextField type="date" label="Hasta" InputLabelProps={{ shrink: true }} value={to} onChange={(e) => setTo(e.target.value)} size="small" />
      <Button variant="contained" startIcon={<SearchIcon />} onClick={() => onSearch(from, to)}>Generar</Button>
    </Stack>
  );
}
