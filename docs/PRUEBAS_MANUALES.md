# Pruebas manuales

## Obtener token

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

## Consultar subtipos naturales

```powershell
Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8082/api/v1/customers/subtypes?customerType=NATURAL" `
  -Headers @{ Authorization = "Bearer $token" }
```

## Crear persona natural

```powershell
$body = @{
  subtypeCode = "NAT_MASIVO"
  identificationType = "CEDULA"
  identification = "1755555555"
  email = "maria.demo@banquito.com"
  mobilePhone = "0995555555"
  address = "Quito"
  names = "María Fernanda"
  lastNames = "Demo Cliente"
  birthDate = "1992-04-10"
  gender = "NO_DECLARA"
  nationality = "ECUATORIANA"
} | ConvertTo-Json

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8082/api/v1/customers/natural-persons" `
  -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $token" } `
  -Body $body
```

## Consultar por identificación

```powershell
Invoke-RestMethod `
  -Method Get `
  -Uri "http://localhost:8082/api/v1/customers/by-identification/1755555555" `
  -Headers @{ Authorization = "Bearer $token" }
```
