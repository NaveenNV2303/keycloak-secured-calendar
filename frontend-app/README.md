# frontend-app

Spring Boot frontend application that uses OIDC (Keycloak) to authenticate users and call a protected `calendar` service.

Place Keycloak configuration in `src/main/resources/application.properties` or via environment variables:

- `KEYCLOAK_ISSUER_URI` e.g. `http://localhost:8080/realms/demo`
- `KEYCLOAK_CLIENT_ID`
- `KEYCLOAK_CLIENT_SECRET` (for confidential clients)
- `CALENDAR_SERVICE_URL` e.g. `http://localhost:8081/calendar`

Run with Maven:

```powershell
cd frontend-app
./mvnw spring-boot:run
```

Note: This module expects Java 21 as set in the POM.
