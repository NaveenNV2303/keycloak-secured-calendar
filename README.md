# Keycloak Secured Calendar

This project demonstrates securing microservices using Keycloak and OpenID Connect (OIDC). It includes two Spring Boot applications:

* **frontend-app**: A user-facing web application that authenticates users via Keycloak and enforces role-based access control (`my-role`).
* **calendar-service**: A backend REST service protected by Keycloak tokens, accessible only to authorized clients.

---

## Features

* OAuth2 / OIDC integration with Keycloak
* Role-based access control on frontend
* Secure communication between frontend and backend using Keycloak-issued tokens
* Separation of concerns with distinct frontend and backend microservices
* Production-ready security best practices
* Environment variable support for sensitive configuration

---

## Prerequisites

* Java 17 or higher
* Maven 3.6 or higher
* Docker (optional, for running Keycloak)
* A running Keycloak instance configured with:

  * Realm (e.g., `frontend-calendar`)
  * Clients (`frontend-app` and `calendar-service`)
  * Roles (e.g., `my-role`)
  * Users assigned appropriate roles

⚠️ Keycloak setup is required before running the applications.
A preconfigured realm export is provided for easy setup.

👉 Follow the detailed instructions in keycloak/README.md

---

## Setup & Run

### 1. Clone the repository

```bash
git clone https://github.com/NaveenNV2303/keycloak-secured-calendar.git
cd keycloak-secured-calendar
```

Access Keycloak admin at [http://localhost:8080](http://localhost:8080)
Login with `admin / adminpassword` (change for production)

### 2. Set environment variable for client secret

Replace `your-client-secret` with your actual **frontend-app** client secret from Keycloak.

**Linux / macOS**

```bash
export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KEYCLOAK_CLIENT_SECRET=your-client-secret
```

**Windows (PowerShell)**

```powershell
$env:SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KEYCLOAK_CLIENT_SECRET="your-client-secret"
```

### 3. Build and run the applications

**Frontend App**

```bash
cd frontend-app
mvn clean install
mvn spring-boot:run
```

**Calendar Service** (run in a new terminal)

```bash
cd calendar-service
mvn clean install
mvn spring-boot:run
```

### 4. Access the frontend application

Open your browser and navigate to:

```
http://localhost:8090
```

* Login with a user assigned the `my-role` role to access calendar data
* Users without the role can log in but will be denied access to secured resources

---

## Cleanup

Stop Spring Boot apps via `Ctrl + C`.

Stop and remove Keycloak Docker container:

```bash
docker stop keycloak
docker rm keycloak
```

---

## Notes

* Use environment variables or a secure vault.
* Adjust Keycloak and application configuration for your production environment.
