import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.jsx";
import "./index.css";
import { ReactKeycloakProvider } from "@react-keycloak/web";
import keycloak from "./services/keycloak";

// keycloak-js usa crypto.randomUUID y crypto.subtle (PKCE), que el navegador solo expone en "contextos
// seguros" (HTTPS o localhost). El despliegue del curso es HTTP sobre IP pública, así que:
//  - randomUUID se reconstruye con crypto.getRandomValues (sí disponible en HTTP)
//  - PKCE se usa solo si el contexto es seguro
if (window.crypto && !window.crypto.randomUUID) {
  window.crypto.randomUUID = () =>
    "10000000-1000-4000-8000-100000000000".replace(/[018]/g, (c) =>
      (c ^ (window.crypto.getRandomValues(new Uint8Array(1))[0] & (15 >> (c / 4)))).toString(16)
    );
}
const pkceMethod = window.isSecureContext ? "S256" : false;

// "login-required": si no hay sesión, Keycloak redirige automáticamente a su pantalla de login
ReactDOM.createRoot(document.getElementById("root")).render(
  <ReactKeycloakProvider authClient={keycloak} initOptions={{ onLoad: "login-required", pkceMethod, checkLoginIframe: false }}>
    <App />
  </ReactKeycloakProvider>
);
