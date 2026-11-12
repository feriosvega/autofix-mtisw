# Microservicios AutoFix

Cada carpeta es un proyecto Spring Boot independiente. Las aplicaciones obtienen su configuración de Config Server,
se registran en Eureka y usan `server.port=0` salvo los servicios de infraestructura.

## Microservicios

- `vehicle-service` (M1): CRUD de vehículos y validación de patente.
- `repair-list-service` (M2): catálogo CRUD y precios por tipo de motor; carga inicial de las 11 reparaciones.
- `repair-service` (M3): registro de reparaciones, precios consultados a M2, vehículo consultado a M1,
  historial HU4 y datos de detalle para reportes.
- `report-service` (M4): R1 por mes/año y R2 con tres meses comparados.
- `config-service`, `eureka-service`, `gateway-service`: patrones requeridos.

## Recursos Kubernetes

El puerto de negocio de M1–M4 sigue siendo efímero. Cada Pod tiene un sidecar Nginx en el puerto interno 8080
para que su Service ClusterIP pueda tener un puerto estable. La aplicación publica su puerto efectivo en el
volumen compartido; Eureka registra ese puerto real y las llamadas RestTemplate usan los registros Eureka.
El sidecar solo adapta el tráfico que llega al Service de Kubernetes.

Los servicios entre backend siguen llamándose por su ID Eureka; no pasan por Gateway. El frontend no conoce las
direcciones internas de M1–M4 y envía todas las llamadas HTTP a `/api`, que Nginx reenvía al Gateway.

