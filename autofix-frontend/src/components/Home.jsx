import Typography from "@mui/material/Typography";
import Paper from "@mui/material/Paper";

export default function Home() {
  return <Paper sx={{ p: 3 }}>
    <Typography variant="h4" gutterBottom>AutoFix: Sistema de Gestión de Reparaciones</Typography>
    <Typography paragraph>Registra vehículos, administra el catálogo de reparaciones, registra los trabajos realizados y consulta el historial completo y los reportes mensuales.</Typography>
    <Typography>La aplicación usa React, microservicios Spring Boot, Eureka, Config Server, API Gateway y bases de datos PostgreSQL independientes.</Typography>
  </Paper>;
}

