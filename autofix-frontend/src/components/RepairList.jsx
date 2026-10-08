import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import repairService from "../services/repair.service";
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
import CalculateIcon from "@mui/icons-material/Calculate";
import VisibilityIcon from "@mui/icons-material/Visibility";
import { errorMessage, money, time } from "../utils/format";

const RepairList = ({ roles }) => {
  const [repairs, setRepairs] = useState([]);
  const [vehicles, setVehicles] = useState({});
  const [error, setError] = useState("");
  const [info, setInfo] = useState("");
  const navigate = useNavigate();
  const isAdmin = roles.includes("ADMIN");

  const init = () => {
    vehicleService
      .getAll()
      .then((r) => setVehicles(Object.fromEntries(r.data.map((v) => [v.id, v]))))
      .catch((e) => setError(errorMessage(e)));
    repairService
      .getAll()
      .then((r) => setRepairs(r.data.sort((a, b) => (a.entryDate < b.entryDate ? 1 : -1))))
      .catch((e) => setError(errorMessage(e)));
  };

  useEffect(() => {
    init();
  }, []);

  // HU3: recalcula el costo total con los datos actuales (ej. después de registrar el retiro)
  const handleCalculate = (id) => {
    repairService
      .calculate(id)
      .then((r) => {
        setInfo(`Costo total recalculado: ${money(r.data.totalAmount)}`);
        init();
      })
      .catch((e) => setError(errorMessage(e)));
  };

  const handleDelete = (id) => {
    if (window.confirm("¿Está seguro que desea eliminar esta reparación?")) {
      repairService.remove(id).then(() => init()).catch((e) => setError(errorMessage(e)));
    }
  };

  return (
    <TableContainer component={Paper} sx={{ p: 2 }}>
      <Typography variant="h5" gutterBottom>Historial de reparaciones</Typography>
      <Link to="/repairs/add" style={{ textDecoration: "none" }}>
        <Button variant="contained" startIcon={<AddIcon />}>Registrar reparación</Button>
      </Link>
      {error && <Alert severity="error" sx={{ mt: 2 }} onClose={() => setError("")}>{error}</Alert>}
      {info && <Alert severity="success" sx={{ mt: 2 }} onClose={() => setInfo("")}>{info}</Alert>}
      <Table size="small" sx={{ mt: 2 }}>
        <TableHead>
          <TableRow>
            {["Patente", "Ingreso", "Salida reparación", "Retiro cliente", "N° rep.", "Monto total", "Operaciones"].map((h) => (
              <TableCell key={h} sx={{ fontWeight: "bold" }}>{h}</TableCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {repairs.map((r) => (
            <TableRow key={r.id}>
              <TableCell>{vehicles[r.vehicleId]?.plate}</TableCell>
              <TableCell>{r.entryDate} {time(r.entryTime)}</TableCell>
              <TableCell>{r.exitDate ? `${r.exitDate} ${time(r.exitTime)}` : "En taller"}</TableCell>
              <TableCell>{r.pickupDate ? `${r.pickupDate} ${time(r.pickupTime)}` : "-"}</TableCell>
              <TableCell>{r.details.length}</TableCell>
              <TableCell sx={{ fontWeight: "bold" }}>{money(r.totalAmount)}</TableCell>
              <TableCell>
                <Button size="small" variant="outlined" startIcon={<VisibilityIcon />} onClick={() => navigate(`/repairs/${r.id}`)}>Detalle</Button>
                <Button size="small" variant="contained" color="info" startIcon={<EditIcon />} sx={{ ml: 1 }}
                        onClick={() => navigate(`/repairs/edit/${r.id}`)}>Editar</Button>
                <Button size="small" variant="contained" color="secondary" startIcon={<CalculateIcon />} sx={{ ml: 1 }}
                        onClick={() => handleCalculate(r.id)}>Calcular</Button>
                {isAdmin && (
                  <Button size="small" variant="contained" color="error" startIcon={<DeleteIcon />} sx={{ ml: 1 }}
                          onClick={() => handleDelete(r.id)}>Eliminar</Button>
                )}
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

export default RepairList;
