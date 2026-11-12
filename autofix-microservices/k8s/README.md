# Despliegue de AutoFix — Evaluación 2

Los manifiestos crean un namespace `autofix`, un PostgreSQL independiente para M1, M2 y M3,
Config Server, Eureka, Gateway, los cuatro microservicios y el frontend. M4 no usa base de datos.
Los servicios M1–M4, Config Server, Eureka y las bases de datos son ClusterIP. Solo el Gateway
se expone como backend mediante NodePort; el frontend tiene su propio NodePort para abrir la web
desde el navegador.

## Imágenes Docker Hub

Inicia sesión en Docker Hub y publica las siete imágenes backend y la imagen del frontend:

```bash
export DOCKERHUB_USER=tu_usuario
docker login
./autofix-microservices/k8s/build-and-push.sh
```

Las imágenes se etiquetan como `autofix-<servicio>:eval2`. El script construye imágenes independientes
desde cada proyecto Spring Boot y desde el frontend.

## Despliegue en Kubernetes

Minikube debe correr dentro de la VM indicada por el enunciado. Desde esa VM, crea la contraseña de
PostgreSQL como variable de entorno y aplica los manifiestos:

```bash
export DOCKERHUB_USER=tu_usuario
export DB_USER=autofix
export DB_PASSWORD='una-clave-segura'
./autofix-microservices/k8s/deploy.sh
kubectl get pods,services -n autofix
```

Abre la aplicación desde el equipo anfitrión usando la IP alcanzable de la VM y el NodePort del frontend,
por ejemplo `http://<ip-de-la-vm>:30081`. El Gateway queda en `http://<ip-de-la-vm>:30080`.
No se usa `port-forward`. Las bases de datos guardan datos en PVC independientes.

Los manifiestos usan el Config Server de la rama `eval2`; publica esa rama antes de iniciar los Pods para
que Config Server pueda clonar `autofix-microservices/config-data` desde GitHub. El Config Server,
Eureka y Gateway usan puertos fijos; M1–M4 mantienen `server.port=0` en la configuración central y
publican su puerto efectivo en Eureka. Las llamadas entre backend y frontend pasan por Gateway; M1/M2/M3/M4
se llaman entre sí con RestTemplate y el nombre del servicio registrado en Eureka.

## Desarrollo local con Docker Compose

```bash
cd autofix-microservices
cp .env.example .env
# Edita DB_PASSWORD en .env
docker-compose up --build
```

La aplicación queda en `http://localhost:8088`. Las tres bases de datos usan contenedores y volúmenes
separados. El Compose de esta carpeta es solo para desarrollo; el despliegue de evaluación es Kubernetes.
