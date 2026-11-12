import axios from "axios";

// Toda petición del frontend va al API Gateway (único punto de acceso al backend).
// En Kubernetes el navegador llama directo al NodePort del gateway: http://<IP minikube>:<GATEWAY_NODE_PORT>.
// Sin puerto configurado (npm run dev / docker compose) se usan rutas relativas /api.
const runtime = window.__AUTOFIX__ || {};
const gatewayUrl = runtime.gatewayPort
  ? `${window.location.protocol}//${window.location.hostname}:${runtime.gatewayPort}`
  : "";

const api = axios.create({
  baseURL: import.meta.env.VITE_BACKEND_URL || gatewayUrl,
  headers: { "Content-Type": "application/json" },
});

export default api;
