import { useState } from "react";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import IconButton from "@mui/material/IconButton";
import Chip from "@mui/material/Chip";
import MenuIcon from "@mui/icons-material/Menu";
import { useKeycloak } from "@react-keycloak/web";
import Sidemenu from "./Sidemenu";

export default function Navbar({ roles }) {
  const [open, setOpen] = useState(false);
  const { keycloak } = useKeycloak();
  const toggleDrawer = (value) => () => setOpen(value);
  const role = roles.includes("ADMIN") ? "ADMIN" : roles.includes("USER") ? "USER" : "SIN ROL";

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <IconButton size="large" edge="start" color="inherit" aria-label="menu" sx={{ mr: 2 }} onClick={toggleDrawer(true)}>
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            AutoFix: Sistema de Gestión de Reparaciones
          </Typography>
          <Typography sx={{ mr: 1 }}>{keycloak.tokenParsed?.preferred_username}</Typography>
          <Chip label={role} color={role === "ADMIN" ? "warning" : "default"} size="small" sx={{ mr: 2, color: "white" }} variant="outlined" />
          <Button color="inherit" onClick={() => keycloak.logout({ redirectUri: window.location.origin })}>
            Logout
          </Button>
        </Toolbar>
      </AppBar>
      <Sidemenu open={open} toggleDrawer={toggleDrawer} roles={roles} />
    </Box>
  );
}
