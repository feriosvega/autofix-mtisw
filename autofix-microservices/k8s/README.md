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

## Despliegue en Kubernetes (minikube en una VM)

El enunciado exige minikube dentro de una máquina virtual. En este equipo se usa KVM (driver kvm2, compatible con Secure Boot):

```bash
minikube start --driver=kvm2 --cpus=4 --memory=6g      # VM KVM (o --driver=virtualbox)
minikube ip                      # IP de la VM: con ella se abre la app en el navegador
DOCKERHUB_USER=tu_usuario ./autofix-microservices/k8s/deploy.sh
kubectl get pods,services -n autofix
```

`deploy.sh` aplica, en orden: namespace, Secret (`01-secrets.yaml`, credenciales de PostgreSQL en
base64), ConfigMap (`01-configmap.yaml`), las 3 bases PostgreSQL con su PVC, Config Server + Eureka +
Gateway y los microservicios M1–M4 y el frontend. Los pods de M1–M4 tienen 2 contenedores: la app
(puerto dinámico) y un nginx que expone el puerto 8080 del Service ClusterIP.

- Frontend: `http://<minikube ip>:30081`
- API Gateway (único NodePort del backend): `http://<minikube ip>:30080`

El navegador llama directamente al Gateway por su NodePort (el frontend lee el puerto desde el
ConfigMap, clave `GATEWAY_NODE_PORT`). No se usa `port-forward`.

Para mostrar el registro en Eureka (puertos asignados por el sistema):

```bash
kubectl -n autofix exec deploy/eureka-service -- wget -qO- --header 'Accept: application/json' http://localhost:8761/eureka/apps
```

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
