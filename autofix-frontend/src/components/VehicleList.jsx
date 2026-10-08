import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import vehicleService from "../services/vehicle.service";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableContainer from "@mui/material/TableContainer";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Paper from "@mui/material/Paper";
import Button from "@mui/material/Button";
import Alert from "@mui/material/Alert";
import Typography from "@mui/material/Typography";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import BuildIcon from "@mui/icons-material/Build";
import { LABELS } from "../utils/constants";
import { errorMessage } from "../utils/format";

const VehicleList = ({ roles }) => {
  const [vehicles, setVehicles] = useState([]);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const isAdmin = roles.includes("ADMIN");

  const init = () => {
    vehicleService
      .getAll()
      .then((response) => setVehicles(response.data))
      .catch((e) => setError(errorMessage(e)));
  };

  useEffect(() => {
    init();
  }, []);

  const handleDelete = (id) => {
    if (window.confirm("¿Está seguro que desea eliminar este vehículo?")) {
      vehicleService
        .remove(id)
        .then(() => init())
        .catch((e) => setError(errorMessage(e)));
    }
  };

  return (
    <TableContainer component={Paper} sx={{ p: 2 }}>
      <Typography variant="h5" gutterBottom>Vehículos registrados</Typography>
      <Link to="/vehicles/add" style={{ textDecoration: "none" }}>
        <Button variant="contained" color="primary" startIcon={<AddIcon />}>Registrar vehículo</Button>
      </Link>
      {error && <Alert severity="error" sx={{ mt: 2 }} onClose={() => setError("")}>{error}</Alert>}
      <Table size="small" sx={{ mt: 2 }}>
        <TableHead>
          <TableRow>
            {["Patente", "Marca", "Modelo", "Tipo", "Año", "Motor", "Asientos", "Operaciones"].map((h) => (
              <TableCell key={h} sx={{ fontWeight: "bold" }}>{h}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {vehicles.map((v) => (
            <TableRow key={v.id}>
              <TableCell>{v.plate}</TableCell>
              <TableCell>{v.brand}</TableCell>
              <TableCell>{v.model}</TableCell>
              <TableCell>{LABELS[v.vehicleType]}</TableCell>
              <TableCell>{v.manufactureYear}</TableCell>
              <TableCell>{LABELS[v.engineType]}</TableCell>
              <TableCell>{v.seats}</TableCell>
              <TableCell>
                <Button size="small" variant="contained" color="success" startIcon={<BuildIcon />}
                        onClick={() => navigate(`/repairs/add?vehicleId=${v.id}`)}>Ingresar</Button>
                <Button size="small" variant="contained" color="info" startIcon={<EditIcon />} sx={{ ml: 1 }}
                        onClick={() => navigate(`/vehicles/edit/${v.id}`)}>Editar</Button>
                {isAdmin && (
                  <Button size="small" variant="contained" color="error" startIcon={<DeleteIcon />} sx={{ ml: 1 }}
                          onClick={() => handleDelete(v.id)}>Eliminar</Button>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default VehicleList;
