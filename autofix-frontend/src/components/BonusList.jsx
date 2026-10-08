import { useEffect, useState } from "react";
import bonusService from "../services/bonus.service";
import Paper from "@mui/material/Paper";
import Grid from "@mui/material/Grid";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import Button from "@mui/material/Button";
import Alert from "@mui/material/Alert";
import Typography from "@mui/material/Typography";
import Stack from "@mui/material/Stack";
import DeleteIcon from "@mui/icons-material/Delete";
import { BONUS_BRANDS } from "../utils/constants";
import { errorMessage, money } from "../utils/format";

const now = new Date();
const EXAMPLE = `- Toyota: 5 bonos de 70.000 pesos
- Ford: 2 bonos de 50.000 pesos
- Hyundai: 1 bono de 30.000 pesos
- Honda: 7 bonos de 40.000 pesos`;

// Bonos del convenio con TopCar: se cargan al inicio de cada mes (manual o pegando el correo)
const BonusList = () => {
  const [bonuses, setBonuses] = useState([]);
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [bonus, setBonus] = useState({ brand: "Toyota", quantity: "", amount: "" });
  const [email, setEmail] = useState(EXAMPLE);
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");

  const init = () => bonusService.getAll().then((r) => setBonuses(r.data)).catch((e) => setError(errorMessage(e)));

  useEffect(() => {
    init();
  }, []);

  const saveOne = () => {
    bonusService
      .create({ ...bonus, bonusYear: year, bonusMonth: month })
      .then(() => { setInfo("Bono registrado"); init(); })
      .catch((e) => setError(errorMessage(e)));
  };

  const saveEmail = () => {
    bonusService
      .fromEmail(email, year, month)
      .then((r) => { setInfo(`${r.data.length} bonos registrados desde el correo`); init(); })
      .catch((e) => setError(errorMessage(e)));
  };

  const remove = (id) => bonusService.remove(id).then(() => init()).catch((e) => setError(errorMessage(e)));

  return (
    <Grid container spacing={2}>
      <Grid item xs={12} md={5}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>Registrar bonos del mes</Typography>
          {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError("")}>{error}</Alert>}
          {info && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setInfo("")}>{info}</Alert>}
          <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
            <TextField label="Año" type="number" value={year} onChange={(e) => setYear(e.target.value)} />
            <TextField label="Mes" type="number" value={month} onChange={(e) => setMonth(e.target.value)} />
          </Stack>
          <Typography variant="subtitle2">Opción 1: pegar el correo de TopCar</Typography>
          <TextField multiline minRows={4} fullWidth value={email} onChange={(e) => setEmail(e.target.value)} sx={{ my: 1 }} />
          <Button variant="contained" onClick={saveEmail}>Cargar desde correo</Button>
          <Typography variant="subtitle2" sx={{ mt: 3 }}>Opción 2: ingresar una marca</Typography>
          <Stack direction="row" spacing={1} sx={{ my: 1 }}>
            <TextField select label="Marca" value={bonus.brand} onChange={(e) => setBonus({ ...bonus, brand: e.target.value })} sx={{ minWidth: 120 }}>
              {BONUS_BRANDS.map((b) => <MenuItem key={b} value={b}>{b}</MenuItem>)}
            </TextField>
            <TextField label="N° bonos" type="number" value={bonus.quantity} onChange={(e) => setBonus({ ...bonus, quantity: e.target.value })} />
            <TextField label="Monto" type="number" value={bonus.amount} onChange={(e) => setBonus({ ...bonus, amount: e.target.value })} />
          </Stack>
          <Button variant="outlined" onClick={saveOne}>Grabar bono</Button>
        </Paper>
      </Grid>
      <Grid item xs={12} md={7}>
        <Paper sx={{ p: 2 }}>
          <Typography variant="h6" gutterBottom>Bonos registrados</Typography>
          <Table size="small">
            <TableHead>
              <TableRow>
                {["Año/Mes", "Marca", "Disponibles", "Monto c/u", ""].map((h) => <TableCell key={h} sx={{ fontWeight: "bold" }}>{h}</TableCell>)}
              </TableRow>
            </TableHead>
            <TableBody>
              {bonuses.map((b) => (
                <TableRow key={b.id}>
                  <TableCell>{b.bonusYear}/{String(b.bonusMonth).padStart(2, "0")}</TableCell>
                  <TableCell>{b.brand}</TableCell>
                  <TableCell>{b.quantity}</TableCell>
                  <TableCell>{money(b.amount)}</TableCell>
                  <TableCell><Button size="small" color="error" startIcon={<DeleteIcon />} onClick={() => remove(b.id)}>Eliminar</Button></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Paper>
      </Grid>
    </Grid>
  );
};

export default BonusList;
