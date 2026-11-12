import { useEffect, useState } from "react";
import repairTypeService from "../services/repair-type.service";
import { errorMessage, money } from "../utils/format";
import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import Alert from "@mui/material/Alert";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import TextField from "@mui/material/TextField";
import Stack from "@mui/material/Stack";

const EMPTY = { name: "", priceGasoline: 0, priceDiesel: 0, priceHybrid: 0, priceElectric: 0 };
const FIELDS = [["priceGasoline", "Gasolina"], ["priceDiesel", "Diésel"], ["priceHybrid", "Híbrido"], ["priceElectric", "Eléctrico"]];

export default function RepairTypeList() {
  const [rows, setRows] = useState([]);
  const [item, setItem] = useState(EMPTY);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState("");
  const load = () => repairTypeService.getAll().then((r) => setRows(r.data)).catch((e) => setError(errorMessage(e)));
  useEffect(() => { load(); }, []);
  const save = () => {
    const request = item.id ? repairTypeService.update(item) : repairTypeService.create(item);
    request.then(() => { setOpen(false); setItem(EMPTY); load(); }).catch((e) => setError(errorMessage(e)));
  };
  const remove = (id) => {
    if (window.confirm("¿Eliminar este tipo de reparación?")) repairTypeService.remove(id).then(load).catch((e) => setError(errorMessage(e)));
  };
  return <Paper sx={{ p: 2 }}>
    <Stack direction="row" justifyContent="space-between" alignItems="center">
      <Typography variant="h5">Catálogo de reparaciones (HU2)</Typography>
      <Button variant="contained" onClick={() => { setItem(EMPTY); setOpen(true); }}>Agregar reparación</Button>
    </Stack>
    {error && <Alert sx={{ mt: 2 }} severity="error" onClose={() => setError("")}>{error}</Alert>}
    <TableContainer sx={{ mt: 2 }}><Table size="small">
      <TableHead><TableRow>{["ID", "Reparación", ...FIELDS.map(([,label]) => label), "Acciones"].map((h) => <TableCell key={h} sx={{ fontWeight: "bold" }}>{h}</TableCell>)}</TableRow></TableHead>
      <TableBody>{rows.map((row) => <TableRow key={row.id}>
        <TableCell>{row.id}</TableCell><TableCell>{row.name}</TableCell>
        {FIELDS.map(([field]) => <TableCell key={field} align="right">{money(row[field])}</TableCell>)}
        <TableCell><Button size="small" onClick={() => { setItem({ ...row }); setOpen(true); }}>Editar</Button>
          <Button size="small" color="error" onClick={() => remove(row.id)}>Eliminar</Button></TableCell>
      </TableRow>)}</TableBody>
    </Table></TableContainer>
    <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
      <DialogTitle>{item.id ? "Editar reparación" : "Nueva reparación"}</DialogTitle>
      <DialogContent><Stack spacing={2} sx={{ pt: 1 }}>
        <TextField label="Nombre" required value={item.name} onChange={(e) => setItem({ ...item, name: e.target.value })} />
        {FIELDS.map(([field, label]) => <TextField key={field} label={`Precio ${label} (pesos)`} type="number" inputProps={{ min: 0, step: 1000 }} value={item[field]} onChange={(e) => setItem({ ...item, [field]: Number(e.target.value) })} />)}
      </Stack></DialogContent>
      <DialogActions><Button onClick={() => setOpen(false)}>Cancelar</Button><Button variant="contained" onClick={save}>Guardar</Button></DialogActions>
    </Dialog>
  </Paper>;
}
