#!/bin/sh
# Escribe config.js con el NodePort del API Gateway (variable GATEWAY_NODE_PORT del ConfigMap)
cat > /usr/share/nginx/html/config.js <<JS
window.__AUTOFIX__ = { gatewayPort: "${GATEWAY_NODE_PORT:-}" };
JS
