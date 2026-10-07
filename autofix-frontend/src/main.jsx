import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.jsx";
import "./index.css";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import keycloak from "./services/keycloak";

// "login-required": si no hay sesión, Keycloak redirige automáticamente a su pantalla de login
ReactDOM.createRoot(document.getElementById("root")).render(
  <ReactKeycloakProvider authClient={keycloak} initOptions={{ onLoad: "login-required", pkceMethod: "S256", checkLoginIframe: false }}>
    <App />
  </ReactKeycloakProvider>
);
