import { useState } from "react";
import AppBar from "@mui/material/AppBar";
import Box from "@mui/material/Box";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import MenuIcon from "@mui/icons-material/Menu";
import Sidemenu from "./Sidemenu";

export default function Navbar() {
  const [open, setOpen] = useState(false);
  const toggleDrawer = (value) => () => setOpen(value);
  return <Box sx={{ flexGrow: 1 }}>
    <AppBar position="static"><Toolbar>
      <IconButton size="large" edge="start" color="inherit" aria-label="menu" sx={{ mr: 2 }} onClick={toggleDrawer(true)}><MenuIcon /></IconButton>
      <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>AutoFix · Evaluación 2</Typography>
    </Toolbar></AppBar>
    <Sidemenu open={open} toggleDrawer={toggleDrawer} roles={["USER", "ADMIN"]} />
  </Box>;
}

