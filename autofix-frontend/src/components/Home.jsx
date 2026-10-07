import Typography from "@mui/material/Typography";
import Paper from "@mui/material/Paper";

const Home = ({ roles }) => {
  const isAdmin = roles.includes("ADMIN");
  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        AutoFix: Sistema de Gestión de Reparaciones
      </Typography>
      <Typography paragraph>
        AutoFix permite registrar los vehículos que llegan por primera vez al taller, registrar sus reparaciones,
        calcular el costo total de cada reparación (con recargos, descuentos, bonos e IVA) y generar reportes.
      </Typography>
      <Typography paragraph>
        Aplicación web monolítica por capas: <b>Spring Boot</b> (backend), <b>React</b> (frontend),{" "}
        <b>PostgreSQL</b> (base de datos) y <b>Keycloak</b> (autenticación y autorización), desplegada con{" "}
        <b>Docker Compose</b> y <b>Nginx</b> como balanceador de carga.
      </Typography>
      <Typography>
        Su rol actual es <b>{isAdmin ? "ADMIN" : "USER"}</b>:{" "}
        {isAdmin
          ? "tiene acceso a todas las historias de usuario (HU1 a HU7)."
          : "tiene acceso a HU1 (vehículos), HU2 (reparaciones), HU3 (cálculo de costos) y HU4 (reporte R1)."}
      </Typography>
    </Paper>
  );
};

export default Home;
