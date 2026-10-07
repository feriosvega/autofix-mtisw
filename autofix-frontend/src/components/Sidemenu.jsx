import Box from "@mui/material/Box";
import Drawer from "@mui/material/Drawer";
import List from "@mui/material/List";
import Divider from "@mui/material/Divider";
import ListSubheader from "@mui/material/ListSubheader";
import ListItemButton from "@mui/material/ListItemButton";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import HomeIcon from "@mui/icons-material/Home";
import DirectionsCarIcon from "@mui/icons-material/DirectionsCar";
import BuildIcon from "@mui/icons-material/Build";
import CardGiftcardIcon from "@mui/icons-material/CardGiftcard";
import ReceiptLongIcon from "@mui/icons-material/ReceiptLong";
import TableChartIcon from "@mui/icons-material/TableChart";
import TimerIcon from "@mui/icons-material/Timer";
import EvStationIcon from "@mui/icons-material/EvStation";
import { useNavigate } from "react-router-dom";

export default function Sidemenu({ open, toggleDrawer, roles }) {
  const navigate = useNavigate();
  const isAdmin = roles.includes("ADMIN");

  // El menú solo muestra las opciones permitidas para el rol (USER: HU1..HU4; ADMIN: todo)
  const options = [
    { section: "General" },
    { label: "Inicio", path: "/home", icon: <HomeIcon /> },
    { section: "Gestión" },
    { label: "Vehículos (HU1)", path: "/vehicles/list", icon: <DirectionsCarIcon /> },
    { label: "Reparaciones (HU2/HU3)", path: "/repairs/list", icon: <BuildIcon /> },
    { label: "Bonos TopCar", path: "/bonuses", icon: <CardGiftcardIcon />, admin: true },
    { section: "Reportes" },
    { label: "R1: Detalle de costos", path: "/reports/r1", icon: <ReceiptLongIcon /> },
    { label: "R2: Reparación x tipo vehículo", path: "/reports/r2", icon: <TableChartIcon />, admin: true },
    { label: "R3: Tiempos de reparación", path: "/reports/r3", icon: <TimerIcon />, admin: true },
    { label: "R4: Reparación x tipo motor", path: "/reports/r4", icon: <EvStationIcon />, admin: true },
  ];

  return (
    <Drawer anchor="left" open={open} onClose={toggleDrawer(false)}>
      <Box sx={{ width: 300 }} role="presentation" onClick={toggleDrawer(false)}>
        <List>
          {options
            .filter((o) => !o.admin || isAdmin)
            .map((o, i) =>
              o.section ? (
                <div key={i}>
                  {i > 0 && <Divider />}
                  <ListSubheader>{o.section}</ListSubheader>
                </div>
              ) : (
                <ListItemButton key={o.path} onClick={() => navigate(o.path)}>
                  <ListItemIcon>{o.icon}</ListItemIcon>
                  <ListItemText primary={o.label} />
                </ListItemButton>
              )
            )}
        </List>
      </Box>
    </Drawer>
  );
}
