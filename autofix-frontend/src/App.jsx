import { BrowserRouter as Router, Navigate, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import Home from "./components/Home";
import NotFound from "./components/NotFound";
import VehicleList from "./components/VehicleList";
import AddEditVehicle from "./components/AddEditVehicle";
import RepairList from "./components/RepairList";
import AddEditRepair from "./components/AddEditRepair";
import RepairDetail from "./components/RepairDetail";
import ReportR1 from "./components/ReportR1";
import ReportR2 from "./components/ReportR2";
import RepairTypeList from "./components/RepairTypeList";
import RepairHistory from "./components/RepairHistory";
import BonusList from "./components/BonusList";
import Footer from "./components/Footer";

// Evaluación 2 sin IAM: todas las acciones habilitadas
const ALL_ROLES = ["USER", "ADMIN"];

function App() {
  return <Router>
    <div className="container">
      <Navbar />
      <div className="page">
        <Routes>
          <Route path="/" element={<Navigate to="/home" />} />
          <Route path="/home" element={<Home />} />
          <Route path="/vehicles/list" element={<VehicleList roles={ALL_ROLES} />} />
          <Route path="/vehicles/add" element={<AddEditVehicle />} />
          <Route path="/vehicles/edit/:id" element={<AddEditVehicle />} />
          <Route path="/repair-types" element={<RepairTypeList />} />
          <Route path="/repairs/list" element={<RepairList roles={ALL_ROLES} />} />
          <Route path="/repairs/add" element={<AddEditRepair />} />
          <Route path="/repairs/edit/:id" element={<AddEditRepair />} />
          <Route path="/bonuses" element={<BonusList />} />
          <Route path="/repairs/history" element={<RepairHistory />} />
          <Route path="/repairs/:id" element={<RepairDetail />} />
          <Route path="/reports/r1" element={<ReportR1 />} />
          <Route path="/reports/r2" element={<ReportR2 />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </div>
      <Footer />
    </div>
  </Router>;
}
export default App;

