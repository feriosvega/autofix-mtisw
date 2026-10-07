import { useEffect, useState } from "react";
import Typography from "@mui/material/Typography";
import httpClient from "../http-common";

// Muestra qué réplica del backend respondió: evidencia del balanceo de carga de Nginx
export default function Footer() {
  const [instance, setInstance] = useState("");

  const refresh = () => {
    httpClient
      .get("/api/v1/public/instance")
      .then((r) => setInstance(r.data.instance))
      .catch(() => setInstance("no disponible"));
  };

  useEffect(() => {
    refresh();
  }, []);

  return (
    <Typography variant="caption" component="div" sx={{ textAlign: "center", color: "gray", py: 2, cursor: "pointer" }} onClick={refresh}>
      AutoFix · Backend atendido por la réplica: <b>{instance}</b> (clic para consultar de nuevo)
    </Typography>
  );
}
