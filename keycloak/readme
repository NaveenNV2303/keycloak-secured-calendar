# Keycloak Configuration

This project uses **Keycloak** for authentication and authorization. To simplify setup, a **preconfigured realm export** is provided.

---

## What’s Included

The realm export (`realm-export.json`) includes:

* Realm configuration
* OAuth2 clients:

  * `frontend-app`
  * `calendar-service`
* Roles (e.g. `my-role`)

❌ **Not included**:

* Users
* Client secrets

This keeps sensitive data out of source control.

---

## Directory Structure

```
keycloak/
├── realm-export.json
└── README.md
```

---

## Import Realm Using Docker (Recommended)

From the project root:

```bash
docker run -d --name keycloak \
  -p 8080:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v $(pwd)/keycloak:/opt/keycloak/data/import \
  quay.io/keycloak/keycloak:21.1.1 start-dev --import-realm
```

### Access Admin Console

* URL: [http://localhost:8080](http://localhost:8080)
* Username: `admin`
* Password: `admin`

This user can now access the secured calendar UI.

---

## Generate Client Secret

1. After importing the realm and logging into the Keycloak Admin Console:
2. Navigate to Clients → frontend-app (or your client)
3. Go to the Credentials tab
4. Click Regenerate Secret to create a new client secret
5. Copy the new secret — you will need it to set the environment variable (SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KEYCLOAK_CLIENT_SECRET) before running the app

---

## Post-Import Checklist

After startup, verify:

* Realm is available (e.g. `frontend-calendar`)
* Clients exist:

  * `frontend-app`
  * `calendar-service`
* Role `my-role` is present
* Redirect URI for `frontend-app`:

```
http://localhost:8090/*
```

---

## Create a Test User

1. Go to **Users → Add user**
2. Set username and password
3. Assign role `my-role`
4. Save

This user can now access the secured calendar UI.

---

## Testing with Postman or API Clients

If you want to test authentication using tools like **Postman**, **curl**, or other API clients, enable **Direct Access Grants** for the relevant client:

1. Go to **Clients → frontend-app** (or the client you want to test)
2. Open **Settings**
3. Enable **Direct Access Grants**
4. Save changes

This allows you to obtain access tokens using the **Resource Owner Password Credentials** flow, which is useful for local testing and debugging.

> ⚠️ Direct Access Grants are **not recommended for production** unless there is a strong use case.

---

## Common Issues

* **Invalid redirect URI** → Check client redirect settings
* **403 Forbidden** → Ensure user has `my-role`
* **Token missing roles** → Verify protocol mappers

---

## Security Notes

* Change admin credentials in production
* Use HTTPS in real deployments
* Store secrets securely (Vault / env vars)
