// Configuración en tiempo de ejecución. En Kubernetes el contenedor reescribe este archivo con el
// NodePort del API Gateway (ConfigMap autofix-config, clave GATEWAY_NODE_PORT).
window.__AUTOFIX__ = { gatewayPort: "" };
