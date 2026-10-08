import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import vehicleService from "../services/vehicle.service";
import Paper from "@mui/material/Paper";
import TextField from "@mui/material/TextField";
import MenuItem from "@mui/material/MenuItem";
import Button from "@mui/material/Button";
import Alert from "@mui/material/Alert";
import Stack from "@mui/material/Stack";
import Typography from "@mui/material/Typography";
import SaveIcon from "@mui/icons-material/Save";
import { ENGINE_TYPES, LABELS, VEHICLE_TYPES } from "../utils/constants";
import { errorMessage } from "../utils/format";

const EMPTY = { plate: "", brand: "", model: "", vehicleType: "SEDAN", manufactureYear: "", engineType: "GASOLINA", seats: 5 };

const AddEditVehicle = () => {
  const [vehicle, setVehicle] = useState(EMPTY);
  const [error, setError] = useState("");
  const { id } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    if (id) {
      vehicleService.get(id).then((r) => setVehicle(r.data)).catch((e) => setError(errorMessage(e)));
    }
  }, [id]);

  const change = (field) => (e) => setVehicle({ ...vehicle, [field]: e.target.value });

  const saveVehicle = (e) => {
    e.preventDefault();
    const request = id ? vehicleService.update(vehicle) : vehicleService.create(vehicle);
    request.then(() => navigate("/vehicles/list")).catch((err) => setError(errorMessage(err)));
  };

  return (
    <Paper sx={{ p: 3, maxWidth: 520, mx: "auto" }} component="form" onSubmit={saveVehicle}>
      <Typography variant="h5" gutterBottom>{id ? "Editar vehículo" : "Registrar vehículo (HU1)"}</Typography>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <Stack spacing={2}>
        <TextField label="Patente" value={vehicle.plate} onChange={change("plate")} required
                   helperText="4 letras y 2 números. Ej: ABCD12" inputProps={{ style: { textTransform: "uppercase" } }} />
        <TextField label="Marca" value={vehicle.brand} onChange={change("brand")} required
                   helperText="Toyota, Kia, Honda, Ford, Chevrolet, Hyundai, etc." />
        <TextField label="Modelo" value={vehicle.model} onChange={change("model")} required />
        <TextField select label="Tipo de vehículo" value={vehicle.vehicleType} onChange={change("vehicleType")}>
          {VEHICLE_TYPES.map((t) => <MenuItem key={t} value={t}>{LABELS[t]}</MenuItem>)}
        </TextField>
        <TextField label="Año de fabricación" type="number" value={vehicle.manufactureYear} onChange={change("manufactureYear")} required />
        <TextField select label="Tipo de motor" value={vehicle.engineType} onChange={change("engineType")}>
          {ENGINE_TYPES.map((t) => <MenuItem key={t} value={t}>{LABELS[t]}</MenuItem>)}
        </TextField>
        <TextField label="Número de asientos" type="number" value={vehicle.seats} onChange={change("seats")} required />
        <Button type="submit" variant="contained" startIcon={<SaveIcon />}>Grabar</Button>
        <Link to="/vehicles/list">Volver al listado</Link>
      </Stack>
    </Paper>
  );
};

export default AddEditVehicle;
