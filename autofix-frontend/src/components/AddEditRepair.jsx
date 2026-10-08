import { useEffect, useState } from "react";
import { Link, useNavigate, useParams, useSearchParams } from "react-router-dom";
import repairService from "../services/repair.service";
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
import { BONUS_BRANDS, LABELS, NOT_FOR_ELECTRIC, REPAIR_NAMES } from "../utils/constants";
import { errorMessage, today } from "../utils/format";

const EMPTY = {
  vehicleId: "", entryDate: today(), entryTime: "10:00", mileage: "",
  exitDate: "", exitTime: "", pickupDate: "", pickupTime: "", applyBonus: false, details: [],
};

const AddEditRepair = () => {
  const [repair, setRepair] = useState(EMPTY);
  const [vehicles, setVehicles] = useState([]);
  const [error, setError] = useState("");
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    vehicleService.getAll().then((r) => setVehicles(r.data)).catch((e) => setError(errorMessage(e)));
    if (id) {
      repairService
        .get(id)
        .then((r) => setRepair({ ...EMPTY, ...nullsToEmpty(r.data) }))
        .catch((e) => setError(errorMessage(e)));
    } else if (searchParams.get("vehicleId")) {
      setRepair((r) => ({ ...r, vehicleId: Number(searchParams.get("vehicleId")) }));
    }
  }, [id]);

  const vehicle = vehicles.find((v) => v.id === repair.vehicleId);
  const selectedTypes = repair.details.map((d) => d.repairType);
  const bonusAllowed = vehicle && BONUS_BRANDS.some((b) => b.toLowerCase() === vehicle.brand.toLowerCase());

  const change = (field) => (e) => setRepair({ ...repair, [field]: e.target.value });

  const toggleRepair = (type) => {
    const details = selectedTypes.includes(type)
      ? repair.details.filter((d) => d.repairType !== type)
      : [...repair.details, { repairType: type, price: 0 }];
    setRepair({ ...repair, details });
  };

  const saveRepair = (e) => {
    e.preventDefault();
    const payload = emptyToNulls({ ...repair, applyBonus: bonusAllowed && repair.applyBonus });
    const request = id ? repairService.update(payload) : repairService.create(payload);
    request.then((r) => navigate(`/repairs/${r.data.id}`)).catch((err) => setError(errorMessage(err)));
  };

  return (
    <Paper sx={{ p: 3, maxWidth: 900, mx: "auto" }} component="form" onSubmit={saveRepair}>
      <Typography variant="h5" gutterBottom>{id ? "Editar reparación" : "Registrar reparación (HU2)"}</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <TextField select fullWidth required label="Vehículo (patente)" value={repair.vehicleId}
                     onChange={(e) => setRepair({ ...repair, vehicleId: e.target.value, details: [] })}>
            {vehicles.map((v) => (
              <MenuItem key={v.id} value={v.id}>{v.plate} - {v.brand} {v.model} ({LABELS[v.engineType]})</MenuItem>
            ))}
          </TextField>
        </Grid>
        <Grid item xs={12} md={6}>
          <TextField fullWidth required type="number" label="Kilometraje" value={repair.mileage} onChange={change("mileage")} />
        </Grid>

        <Grid item xs={6} md={3}>
          <TextField fullWidth required type="date" label="Fecha ingreso" InputLabelProps={{ shrink: true }}
                     value={repair.entryDate} onChange={change("entryDate")} />
        </Grid>
        <Grid item xs={6} md={3}>
          <TextField fullWidth required type="time" label="Hora ingreso" InputLabelProps={{ shrink: true }}
                     value={repair.entryTime} onChange={change("entryTime")} />
        </Grid>
        <Grid item xs={6} md={3}>
          <TextField fullWidth type="date" label="Fecha salida reparación" InputLabelProps={{ shrink: true }}
                     value={repair.exitDate} onChange={change("exitDate")} />
        </Grid>
        <Grid item xs={6} md={3}>
          <TextField fullWidth type="time" label="Hora salida reparación" InputLabelProps={{ shrink: true }}
                     value={repair.exitTime} onChange={change("exitTime")} />
        </Grid>
        <Grid item xs={6} md={3}>
          <TextField fullWidth type="date" label="Fecha retiro cliente" InputLabelProps={{ shrink: true }}
                     value={repair.pickupDate} onChange={change("pickupDate")} />
        </Grid>
        <Grid item xs={6} md={3}>
          <TextField fullWidth type="time" label="Hora retiro cliente" InputLabelProps={{ shrink: true }}
                     value={repair.pickupTime} onChange={change("pickupTime")} />
        </Grid>
        <Grid item xs={12} md={6} sx={{ display: "flex", alignItems: "center" }}>
          <FormControlLabel
            control={<Checkbox checked={!!repair.applyBonus} disabled={!bonusAllowed}
                               onChange={(e) => setRepair({ ...repair, applyBonus: e.target.checked })} />}
            label={bonusAllowed ? `Aplicar bono TopCar (${vehicle.brand})` : "Bono TopCar (solo Toyota, Ford, Hyundai, Honda)"}
          />
        </Grid>
      </Grid>

      <Divider sx={{ my: 2 }} />
      <Typography variant="h6">Reparaciones realizadas</Typography>
      <FormGroup sx={{ columns: { md: 2 } }}>
        {Object.entries(REPAIR_NAMES).map(([type, name]) => {
          const t = Number(type);
          const disabled = vehicle?.engineType === "ELECTRICO" && NOT_FOR_ELECTRIC.includes(t);
          return (
            <FormControlLabel key={t} disabled={disabled || !vehicle}
                              control={<Checkbox checked={selectedTypes.includes(t)} onChange={() => toggleRepair(t)} />}
                              label={`${t}. ${name}${disabled ? " (no aplica a eléctricos)" : ""}`} />
          );
        })}
      </FormGroup>

      <Button type="submit" variant="contained" startIcon={<SaveIcon />} sx={{ mt: 2 }}>
        Grabar y calcular costo
      </Button>
      <div style={{ marginTop: 12 }}><Link to="/repairs/list">Volver al listado</Link></div>
    </Paper>
  );
};

// El backend usa null para fechas/horas vacías; el formulario usa ""
const nullsToEmpty = (r) => Object.fromEntries(Object.entries(r).map(([k, v]) => [k, v === null ? "" : v]));
const emptyToNulls = (r) => Object.fromEntries(Object.entries(r).map(([k, v]) => [k, v === "" ? null : v]));

export default AddEditRepair;
