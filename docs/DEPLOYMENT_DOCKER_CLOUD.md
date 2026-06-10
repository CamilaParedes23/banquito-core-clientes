# core-customer-service - despliegue Docker / nube

## Objetivo

Este microservicio queda preparado para ejecutarse localmente, en Docker Compose y en nube sin modificar código fuente. Toda configuración sensible o dependiente del entorno debe venir por variables de entorno.

## Variables principales

| Variable | Uso | Local | Docker/Nube |
|---|---|---|---|
| `SERVER_PORT` | Puerto HTTP | `8082` | `8082` |
| `CUSTOMER_DB_URL` | JDBC MySQL Customer | `localhost:33063` | `mysql-customer:3306` |
| `CUSTOMER_DB_USER` | Usuario BD | `root` | secreto/env |
| `CUSTOMER_DB_PASSWORD` | Password BD | local | secreto/env |
| `JWT_ISSUER` | Emisor de tokens | `banquito-identity-access` | igual |
| `JWT_SECRET` | Secreto compartido con Identity | local | secreto/env |
| `DEMO_DATA_ENABLED` | Seed demo | `true` en laboratorio | `false` recomendado |
| `GRPC_SERVER_PORT` | Puerto gRPC reservado | `9092` | `9092` |

## Build

```powershell
mvn clean package
docker build -t banquito/core-customer-service:0.0.1 .
```

## Run local Docker

```powershell
docker run --rm -p 8082:8082 --env-file .env.example banquito/core-customer-service:0.0.1
```

## Kong

Este servicio no implementa gateway propio. Se expone externamente por Kong mediante REST/OpenAPI. La comunicación interna Core -> Customer debe resolverse por gRPC durante la fase de integración interna.

## Ruta pública por Kong

```text
/api/v1/customers/**
```

## Ruta interna actual del servicio

```text
http://core-customer-service:8082/api/v1/customers/**
```
