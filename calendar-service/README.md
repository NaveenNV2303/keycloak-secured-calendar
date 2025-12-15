# calendar-service

A simple Spring Boot resource server that exposes `/calendar` and requires a valid Keycloak-issued JWT.

Configuration (env or application.properties):
- `KEYCLOAK_ISSUER_URI` - e.g. `http://localhost:8080/realms/demo`

Run with Maven:

```powershell
cd calendar-service
mvn -DskipTests=true spring-boot:run
```

The service will validate incoming Bearer tokens against the issuer's JWKS endpoint and only allow authenticated requests.
