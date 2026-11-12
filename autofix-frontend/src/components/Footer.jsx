import Typography from "@mui/material/Typography";

export default function Footer() {
  return (
    <Typography variant="caption" component="div" sx={{ textAlign: "center", color: "gray", py: 2 }}>
      AutoFix · Evaluación 2 · Frontend → API Gateway → microservicios (Eureka + Config Server) en Kubernetes
    </Typography>
  );
}
