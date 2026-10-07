import Keycloak from "keycloak-js";

// Keycloak se publica detrás del mismo Nginx en la ruta /auth (ej. http://<ip-servidor>/auth)
const keycloakUrl = import.meta.env.VITE_KEYCLOAK_URL || `${window.location.origin}/auth`;

const keycloak = new Keycloak({
  url: keycloakUrl,
  realm: "autofix-realm",
  clientId: "autofix-frontend",
});

export default keycloak;
