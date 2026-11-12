import { useEffect, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import repairService from "../services/repair.service";
import repairTypeService from "../services/repair-type.service";
import vehicleService from "../services/vehicle.service";
import Paper from "@mui/material/Paper";
import Grid from "@mui/material/Grid";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import Button from "@mui/material/Button";
import Alert from "@mui/material/Alert";
import Typography from "@mui/material/Typography";
import FormGroup from "@mui/material/FormGroup";
import FormControlLabel from "@mui/material/FormControlLabel";
import Checkbox from "@mui/material/Checkbox";
import Divider from "@mui/material/Divider";
import SaveIcon from "@mui/icons-material/Save";
import { LABELS } from "../utils/constants";
import { errorMessage, today, money } from "../utils/format";

const EMPTY = { vehicleId: "", entryDate: today(), entryTime: "10:00", mileage: "", exitDate: "", exitTime: "", pickupDate: "", pickupTime: "", applyBonus: false, details: [] };
const PRICE_FIELDS = { GASOLINA: "priceGasoline", DIESEL: "priceDiesel", HIBRIDO: "priceHybrid", ELECTRICO: "priceElectric" };

export default function AddEditRepair() {
  const [repair, setRepair] = useState(EMPTY);
  const [vehicles, setVehicles] = useState([]);
  const [repairTypes, setRepairTypes] = useState([]);
  const [error, setError] = useState("");
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    vehicleService.getAll().then((r) => setVehicles(r.data)).catch((e) => setError(errorMessage(e)));
    repairTypeService.getAll().then((r) => setRepairTypes(r.data)).catch((e) => setError(errorMessage(e)));
    if (id) repairService.get(id).then((r) => setRepair({ ...EMPTY, ...nullsToEmpty(r.data) })).catch((e) => setError(errorMessage(e)));
    else if (searchParams.get("vehicleId")) setRepair((r) => ({ ...r, vehicleId: Number(searchParams.get("vehicleId")) }));
  }, [id]);

  const vehicle = vehicles.find((v) => Number(v.id) === Number(repair.vehicleId));
  const selectedTypes = repair.details.map((detail) => Number(detail.repairType));
  const change = (field) => (e) => setRepair({ ...repair, [field]: e.target.value });
  const toggleRepair = (repairType) => {
    const typeId = Number(repairType);
    const details = selectedTypes.includes(typeId)
      ? repair.details.filter((detail) => Number(detail.repairType) !== typeId)
      : [...repair.details, { repairType: typeId, repairDate: repair.entryDate, repairTime: repair.entryTime }];
    setRepair({ ...repair, details });
  };

  const saveRepair = (e) => {
    e.preventDefault();
    const payload = emptyToNulls({
      ...repair,
      vehicleId: Number(repair.vehicleId),
      mileage: Number(repair.mileage),
      details: repair.details.map((detail) => ({ repairType: Number(detail.repairType), repairDate: detail.repairDate || repair.entryDate, repairTime: detail.repairTime || repair.entryTime })),
    });
    const request = id ? repairService.update({ ...payload, id: Number(id) }) : repairService.create(payload);
    request.then((r) => navigate("/repairs/" + r.data.id)).catch((err) => setError(errorMessage(err)));
  };

  const priceFor = (type) => vehicle ? Number(type[PRICE_FIELDS[vehicle.engineType]] || 0) : 0;
  return (
    <Paper sx={{ p: 3, maxWidth: 900, mx: "auto" }} component="form" onSubmit={saveRepair}>
      <Typography variant="h5" gutterBottom>{id ? "Editar reparación" : "Registrar reparación (HU3)"}</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <TextField select fullWidth required label="Vehículo (patente)" value={repair.vehicleId}
            onChange={(e) => setRepair({ ...repair, vehicleId: e.target.value, details: [] })}>
            {vehicles.map((v) => <MenuItem key={v.id} value={v.id}>{v.plate} - {v.brand} {v.model} ({LABELS[v.engineType]})</MenuItem>)}
          </TextField>
        </Grid>
        <Grid item xs={12} md={6}><TextField fullWidth required type="number" inputProps={{ min: 0 }} label="Kilometraje" value={repair.mileage} onChange={change("mileage")} /></Grid>
        <Grid item xs={6} md={3}><TextField fullWidth required type="date" label="Fecha ingreso" InputLabelProps={{ shrink: true }} value={repair.entryDate} onChange={change("entryDate")} /></Grid>
        <Grid item xs={6} md={3}><TextField fullWidth required type="time" label="Hora ingreso" InputLabelProps={{ shrink: true }} value={repair.entryTime} onChange={change("entryTime")} /></Grid>
        <Grid item xs={6} md={3}><TextField fullWidth type="date" label="Fecha salida listo" InputLabelProps={{ shrink: true }} value={repair.exitDate} onChange={change("exitDate")} /></Grid>
        <Grid item xs={6} md={3}><TextField fullWidth type="time" label="Hora salida" InputLabelProps={{ shrink: true }} value={repair.exitTime} onChange={change("exitTime")} /></Grid>
        <Grid item xs={6} md={3}><TextField fullWidth type="date" label="Fecha retiro cliente" InputLabelProps={{ shrink: true }} value={repair.pickupDate} onChange={change("pickupDate")} /></Grid>
        <Grid item xs={6} md={3}><TextField fullWidth type="time" label="Hora retiro" InputLabelProps={{ shrink: true }} value={repair.pickupTime} onChange={change("pickupTime")} /></Grid>
        <Grid item xs={12}>
          <FormControlLabel
            control={<Checkbox checked={!!repair.applyBonus} onChange={(e) => setRepair({ ...repair, applyBonus: e.target.checked })} />}
            label="Aplicar bono TopCar de la marca (Toyota, Ford, Hyundai, Honda). Se descuenta al registrar el retiro." />
        </Grid>
      </Grid>
      <Divider sx={{ my: 2 }} />
      <Typography variant="h6">Reparaciones del catálogo</Typography>
      {!vehicle && <Typography color="text.secondary">Seleccione un vehículo para ver precios según su motor.</Typography>}
      <FormGroup sx={{ columns: { md: 2 } }}>
        {repairTypes.map((type) => {
          const price = priceFor(type);
          const disabled = !vehicle || price <= 0;
          return <FormControlLabel key={type.id} disabled={disabled}
            control={<Checkbox checked={selectedTypes.includes(Number(type.id))} onChange={() => toggleRepair(type.id)} />}
            label={type.id + ". " + type.name + " · " + (disabled ? "No disponible" : money(price))} />;
        })}
      </FormGroup>
      {repair.details.map((detail) => {
        const type = repairTypes.find((item) => Number(item.id) === Number(detail.repairType));
        return <Grid container spacing={2} key={detail.repairType} sx={{ mt: 0.5 }}>
          <Grid item xs={12} md={4}><Typography sx={{ pt: 1 }}>{type?.name || "Reparación"}</Typography></Grid>
          <Grid item xs={6} md={4}><TextField fullWidth required type="date" label="Fecha de reparación" InputLabelProps={{ shrink: true }} value={detail.repairDate || repair.entryDate} onChange={(e) => setRepair({ ...repair, details: repair.details.map((d) => d.repairType === detail.repairType ? { ...d, repairDate: e.target.value } : d) })} /></Grid>
          <Grid item xs={6} md={4}><TextField fullWidth required type="time" label="Hora de reparación" InputLabelProps={{ shrink: true }} value={detail.repairTime || repair.entryTime} onChange={(e) => setRepair({ ...repair, details: repair.details.map((d) => d.repairType === detail.repairType ? { ...d, repairTime: e.target.value } : d) })} /></Grid>
        </Grid>;
      })}
      <Button type="submit" variant="contained" startIcon={<SaveIcon />} sx={{ mt: 2 }}>Grabar reparaciones</Button>
      <div style={{ marginTop: 12 }}><Link to="/repairs/list">Volver al listado</Link></div>
    </Paper>
  );
}
const nullsToEmpty = (record) => Object.fromEntries(Object.entries(record).map(([key, value]) => [key, value === null ? "" : value]));
const emptyToNulls = (record) => Object.fromEntries(Object.entries(record).map(([key, value]) => [key, value === "" ? null : value]));
