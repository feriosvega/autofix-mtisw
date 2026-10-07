import axios from "axios";
import keycloak from "./services/keycloak";

// Vacío => rutas relativas: Nginx enruta /api/ hacia el balanceador del backend
const backendUrl = import.meta.env.VITE_BACKEND_URL || "";

const api = axios.create({
  baseURL: backendUrl,
  headers: {
    "Content-Type": "application/json",
  },
});

// Interceptor: antes de cada petición renueva el token si vence en < 30 s y lo adjunta como Bearer
api.interceptors.request.use(
  async (config) => {
    if (keycloak.authenticated) {
      await keycloak.updateToken(30);
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
