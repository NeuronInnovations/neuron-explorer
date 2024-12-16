# explorer

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

It requires Java 17+ to run do to external dependencies which have been built with Class Major Version 61.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev -Dquarkus.analytics.disabled=true
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

# Building the Application with the Kubernetes Profile

To build the application using the Kubernetes profile, execute the following command:

```bash
mvn clean install -Dquarkus.profile=kubernetes
```

## create a cluster
TDB.

## Installing keycloak using helm on kubernetes


To install for non https  use
```
helm install explorer-keycloak oci://registry-1.docker.io/bitnamicharts/keycloak
``

and manually create a load balancer with an external public ip if you need it


To install for https  use
```
helm install explorer-keycloak oci://registry-1.docker.io/bitnamicharts/keycloak --set ingress-enabled=true --set proxy=edge
``

and manually install a ingress with https load balancer and signed cetificate (eitehr self signed or from your domain registrar)

In either case when installation completes, the kc will appear as a workload and to get the password of the installtion you need
to do this

```
kubectl get secret --namespace default explorer-keycloak -o jsonpath="{.data.admin-password}" | base64 --decode
```
The command above will print the password on the screen and the  username is "user"


# Configuration for Quarkus Application

## Logging
- **`quarkus.log.console.format`**  
  Configures the log format for console output.  
  Example: `%d{yyyy-MM-dd HH:mm:ss} %-5p [%c:%L] (%t) %s%e%n`
    - `%d` - Timestamp in `yyyy-MM-dd HH:mm:ss` format.
    - `%-5p` - Log level (e.g., INFO, DEBUG).
    - `[%c:%L]` - Logger name and line number.
    - `(%t)` - Thread name.
    - `%s` - Log message.
    - `%e` - Exception details.
    - `%n` - Newline.

---

## Authentication Server
- **`auth.host`**  
  The hostname of the authentication server.

- **`auth.protocol`**  
  The protocol used (e.g., `http` or `https`).

- **`auth.port`**  
  The port of the authentication server.

---

## PostgreSQL Database
- **`postgres.host`**  
  The hostname of the PostgreSQL server.

- **`postgres.protocol`**  
  The protocol to access the database (e.g., `jdbc:postgresql`).

- **`postgres.port`**  
  The port of the PostgreSQL server.

---

## Quarkus Features
- **`quarkus.live-reload.instrumentation`**  
  Enables live reload instrumentation for development. Default: `true`.

- **`quarkus.http.cors`**  
  Enables Cross-Origin Resource Sharing (CORS). Default: `true`.

- **`quarkus.http.cors.headers`**  
  Specifies allowed HTTP headers in CORS requests.  
  Example: `accept, origin, authorization, content-type, x-requested-with`.

- **`quarkus.http.cors.methods`**  
  Specifies allowed HTTP methods for CORS.  
  Example: `GET, POST, OPTIONS, PUT, DELETE`.

- **`quarkus.http.proxy.proxy-address-forwarding`**  
  Enables proxy address forwarding for HTTP requests.

- **`quarkus.http.proxy.enable-forwarded-headers`**  
  Allows forwarding of HTTP headers from proxies.

---

## Hedera Hashgraph
- **`hh.private_key`**  
  Private key for the Hedera account.

- **`hh.eth_private_key`**  
  Ethereum private key.

- **`hh.operator_id`**  
  The operator account ID for Hedera.

- **`hh.network`**  
  Network type for Hedera (`TESTNET` or `MAINNET`). Default: `TESTNET`.

- **`hh.contract_id`**  
  Hedera contract ID.

- **`hh.eth_contract_id`**  
  Ethereum contract ID.

---

## OpenID Connect (OIDC)
- **`quarkus.oidc.application-type`**  
  Specifies the type of OIDC application. Example: `web-app`.

- **`quarkus.oidc.auth-server-url`**  
  URL of the authentication server. Example: `${auth.protocol}://${auth.host}:${auth.port}/realms/<your-realm-name>`.

- **`quarkus.oidc.token-state-manager.encryption-required`**  
  Indicates whether token encryption is required. Default: `true`.

- **`quarkus.oidc.token-state-manager.split-tokens`**  
  Enables splitting of large tokens. Default: `true`.

- **`quarkus.oidc.client-id`**  
  Client ID for the OIDC client.

- **`quarkus.oidc.logout.path`**  
  Path for the logout endpoint. Default: `/logout`.

- **`quarkus.oidc.logout.post-logout-path`**  
  Path to redirect after logout. Default: `/`.

- **`quarkus.oidc.authentication.java-script-auto-redirect`**  
  Enables/disables automatic JavaScript redirection. Default: `false`.

---

## Authorization
- **Authenticated Paths**  
  **`quarkus.http.auth.permission.authenticated.paths`**  
  Specifies paths requiring authentication. Example: `/account/*, /devicedetails/*`.

  **`quarkus.http.auth.permission.authenticated.policy`**  
  Policy for authenticated paths. Example: `authenticated`.

- **Public Paths**  
  **`quarkus.http.auth.permission.public.paths`**  
  Specifies paths accessible without authentication. Example: `/*, /register/*`.

  **`quarkus.http.auth.permission.public.policy`**  
  Policy for public paths. Example: `permit`.

---

## Database Configuration
- **`quarkus.datasource.db-kind`**  
  Type of database (e.g., `postgresql`).

- **`quarkus.datasource.username`**  
  Username for database access.

- **`quarkus.datasource.password`**  
  Password for database access.

- **`quarkus.datasource.jdbc.url`**  
  JDBC URL for the database. Example: `${postgres.protocol}://${postgres.host}:${postgres.port}/`.

- **`quarkus.hibernate-orm.database.generation`**  
  Specifies the database schema generation strategy. Default: `update`.

---

## Keycloak Admin
- **`keycloack-admin-cli-secret`**  
  Secret for Keycloak admin CLI.

# Configuration for Quarkus Kubernetes Integration

## Kubernetes Deployment
- **`quarkus.kubernetes.replicas`**  
  Specifies the number of replicas for the Kubernetes deployment.  
  Example: `1`.

---

## Kubernetes Configurations
- **`quarkus.kubernetes-config.enabled`**  
  Enables or disables Kubernetes ConfigMap integration.  
  Default: `false`.

- **`quarkus.kubernetes-config.secrets.enabled`**  
  Enables or disables the use of Kubernetes Secrets for configuration.  
  Default: `false`.

---

## Container Image
- **`quarkus.container-image.registry`**  
  Specifies the container image registry (e.g., Docker Hub, Quay.io).

- **`quarkus.container-image.group`**  
  Specifies the group/namespace for the container image.

- **`quarkus.container-image.name`**  
  Specifies the name of the container image.

- **`quarkus.container-image.push`**  
  Indicates whether the container image should be pushed to the registry.  
  Default: `true`.

---

## Kubernetes Application Metadata
- **`quarkus.kubernetes.name`**  
  Specifies the name of the Kubernetes application.

---

## Kubernetes Client Configuration
- **`quarkus.kubernetes-client.trust-certs`**  
  Indicates whether to trust self-signed or untrusted certificates.  
  Default: `true`.

- **`quarkus.kubernetes-client.namespace`**  
  Specifies the Kubernetes namespace for the application.  
  Default: `default`.

---

## Kubernetes Ports
- **`quarkus.kubernetes.ports.host-port`**  
  Host port for the Kubernetes service.

- **`quarkus.kubernetes.ports.container-port`**  
  Container port exposed by the Kubernetes deployment.

---

## Kubernetes Deployment
- **`quarkus.kubernetes.deploy`**  
  Indicates whether the application should be deployed to Kubernetes.  
  Default: `true`.

- **`quarkus.kubernetes.deployment-target`**  
  Specifies the target deployment platform. Example: `kubernetes`.
