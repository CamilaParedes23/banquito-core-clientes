# core-customer-service

Microservicio de Banco BanQuito V2 responsable de la gestión de clientes naturales, clientes jurídicos, representantes legales y relaciones entre clientes.

## Rol arquitectónico

`core-customer-service` pertenece al Core bancario y administra el bounded context de datos maestros de clientes.

No administra cuentas, saldos, transacciones, credenciales, roles, permisos ni contabilidad. Esos elementos pertenecen a otros microservicios.

## Stack

- JDK 21 LTS
- Spring Boot 4.0.6
- Maven
- MySQL 8.4 LTS
- Flyway
- JPA / Hibernate
- Spring Security con validación JWT
- OpenAPI / Swagger UI
- Preparado para Docker/Kong/nube

## Base package

```text
com.banquito.core.customer
```

## Base de datos

```text
banquito_core_customer_db
Puerto local Docker infraestructura: 33063
```

## Variables clave

Ver `.env.example`.

Variables principales:

```text
SERVER_PORT
CUSTOMER_DB_URL
CUSTOMER_DB_USER
CUSTOMER_DB_PASSWORD
JWT_ISSUER
JWT_SECRET
DEMO_DATA_ENABLED
GRPC_SERVER_PORT
```

## Ejecutar localmente con Maven

```powershell
cd C:anquito-core\core-customer-service
mvn clean package
mvn spring-boot:run
```

## Ejecutar con Docker

```powershell
mvn clean package
docker build -t banquito/core-customer-service:0.0.1 .
docker run --rm -p 8082:8082 --env-file .env.example banquito/core-customer-service:0.0.1
```

## URLs locales

```text
Health:     http://localhost:8082/actuator/health
Swagger:    http://localhost:8082/swagger-ui.html
OpenAPI:    http://localhost:8082/api-docs
```

## Exposición por Kong

Este microservicio no implementa gateway propio. Para exposición externa se debe publicar en Kong:

```text
/api/v1/customers/** -> core-customer-service:8082
```

## Comunicación interna

Según directriz del proyecto:

```text
Core interno -> Core interno = gRPC
Core <-> Switch = REST/OpenAPI vía Kong
```

Para Customer, el consumo interno principal será:

```text
core-account-service -> core-customer-service
```

Uso previsto:

- validar cliente activo;
- obtener cliente por UUID;
- obtener cliente por identificación/RUC;
- validar empresa con pagos masivos habilitados;
- consultar relaciones/representante legal.

## Seguridad

Todos los endpoints de negocio requieren JWT emitido por `identity-access-service`.

Obtener token:

```powershell
$loginBody = @{
  username = "admin.core"
  password = "password"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8081/api/v1/auth/login" `
  -ContentType "application/json" `
  -Body $loginBody

$token = $loginResponse.accessToken
```

## Endpoints principales

```text
GET    /api/v1/customers/subtypes?customerType=NATURAL
POST   /api/v1/customers/natural-persons
POST   /api/v1/customers/legal-persons
GET    /api/v1/customers/{customerUuid}
GET    /api/v1/customers/by-identification/{identification}
PATCH  /api/v1/customers/{customerUuid}
PATCH  /api/v1/customers/{customerUuid}/status
PATCH  /api/v1/customers/{customerUuid}/mass-payments-status
POST   /api/v1/customers/{customerUuid}/relationships
GET    /api/v1/customers/{customerUuid}/relationships
```

## Validaciones rápidas

```powershell
Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8082/api/v1/customers/subtypes?customerType=NATURAL" `
  -Headers @{ Authorization = "Bearer $token" }
```

```powershell
Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8082/api/v1/customers/by-identification/1755555555" `
  -Headers @{ Authorization = "Bearer $token" }
```

## Manejo de errores

El servicio usa:

- `BusinessException` para reglas de negocio;
- `@RestControllerAdvice` para respuestas homogéneas;
- `RestAuthenticationEntryPoint` para 401;
- `RestAccessDeniedHandler` para 403.

Formato estándar:

```json
{
  "timestamp": "2026-06-03T00:00:00",
  "correlationId": "uuid",
  "code": "CUSTOMER_NOT_FOUND",
  "message": "Cliente no encontrado",
  "details": []
}
```

## Datos demo

Se crean datos demo mínimos con `DemoDataInitializer` si `DEMO_DATA_ENABLED=true`:

- persona natural representante legal;
- persona natural adicional;
- persona jurídica habilitada para pagos masivos;
- relación `REPRESENTANTE_LEGAL`.

En nube o ambientes formales se recomienda `DEMO_DATA_ENABLED=false` y usar seeds controlados por Flyway o scripts de carga definidos por volumetría del docente.
