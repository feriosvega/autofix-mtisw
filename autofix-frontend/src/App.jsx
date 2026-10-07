import { BrowserRouter as Router, Navigate, Route, Routes } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";
import Navbar from "./components/Navbar";
import Home from "./components/Home";
import NotFound from "./components/NotFound";
import VehicleList from "./components/VehicleList";
import AddEditVehicle from "./components/AddEditVehicle";
import RepairList from "./components/RepairList";
import AddEditRepair from "./components/AddEditRepair";
import RepairDetail from "./components/RepairDetail";
import BonusList from "./components/BonusList";
import ReportR1 from "./components/ReportR1";
import MatrixReport from "./components/MatrixReport";
import ReportR3 from "./components/ReportR3";
import Footer from "./components/Footer";

const ALL = ["USER", "ADMIN"];
const ADMIN = ["ADMIN"];

// Protege una ruta según los roles del token (la seguridad real la aplica el backend con @PreAuthorize)
const PrivateRoute = ({ element, rolesAllowed, roles }) => {
  if (rolesAllowed && !rolesAllowed.some((r) => roles.includes(r))) {
    return <h2>No tienes permiso para ver esta página</h2>;
  }
  return element;
};

function App() {
  const { keycloak, initialized } = useKeycloak();

  if (!initialized) return <div className="page">Cargando...</div>;

  if (!keycloak.authenticated) {
    keycloak.login();
    return null;
  }

  const roles = keycloak.tokenParsed?.realm_access?.roles || [];

  return (
    <Router>
      <div className="container">
        <Navbar roles={roles} />
        <div className="page">
          <Routes>
            <Route path="/" element={<Navigate to="/home" />} />
            <Route path="/home" element={<Home roles={roles} />} />

            {/* HU1 - Registro de vehículos */}
            <Route path="/vehicles/list" element={<PrivateRoute roles={roles} element={<VehicleList roles={roles} />} rolesAllowed={ALL} />} />
            <Route path="/vehicles/add" element={<PrivateRoute roles={roles} element={<AddEditVehicle />} rolesAllowed={ALL} />} />
            <Route path="/vehicles/edit/:id" element={<PrivateRoute roles={roles} element={<AddEditVehicle />} rolesAllowed={ALL} />} />

            {/* HU2 - Registro de reparaciones / HU3 - Cálculo del costo total */}
            <Route path="/repairs/list" element={<PrivateRoute roles={roles} element={<RepairList roles={roles} />} rolesAllowed={ALL} />} />
            <Route path="/repairs/add" element={<PrivateRoute roles={roles} element={<AddEditRepair />} rolesAllowed={ALL} />} />
            <Route path="/repairs/edit/:id" element={<PrivateRoute roles={roles} element={<AddEditRepair />} rolesAllowed={ALL} />} />
            <Route path="/repairs/:id" element={<PrivateRoute roles={roles} element={<RepairDetail />} rolesAllowed={ALL} />} />

            {/* Bonos TopCar (administración) */}
            <Route path="/bonuses" element={<PrivateRoute roles={roles} element={<BonusList />} rolesAllowed={ADMIN} />} />

            {/* HU4..HU7 - Reportes */}
            <Route path="/reports/r1" element={<PrivateRoute roles={roles} element={<ReportR1 />} rolesAllowed={ALL} />} />
            <Route path="/reports/r2" element={<PrivateRoute roles={roles} element={<MatrixReport report="r2" />} rolesAllowed={ADMIN} />} />
            <Route path="/reports/r3" element={<PrivateRoute roles={roles} element={<ReportR3 />} rolesAllowed={ADMIN} />} />
            <Route path="/reports/r4" element={<PrivateRoute roles={roles} element={<MatrixReport report="r4" />} rolesAllowed={ADMIN} />} />

            <Route path="*" element={<NotFound />} />
          </Routes>
        </div>
        <Footer />
      </div>
    </Router>
  );
}

export default App;
