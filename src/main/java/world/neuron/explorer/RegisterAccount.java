package world.neuron.explorer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.neuron.account.Account;
import world.neuron.keycloak.auth.KeyCloakSettings;
import world.neuron.keycloak.auth.RealmSensorRole;

import java.util.Collections;

@Path("/register")
@ApplicationScoped
public class RegisterAccount {

    Logger logger = LoggerFactory.getLogger(RegisterAccount.class);

    @ConfigProperty(name = "keycloack-admin-cli-secret")
    String keycloakAdminCliSecret;

    @Inject
    KeyCloakSettings keyCloakSettings;

    @Inject
    Template register;

    Keycloak keycloak;

    @PreDestroy
    public void closeKeycloak() {
        keycloak.close();
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderRegister() throws Exception {

        return register.instance()
                .data("pagename", "register");
    }

    @POST
    public Account registerAccount() {
        return null;
    }

    @GET
    @Path("/test")
    public String registrationTest() {

        keycloak = KeycloakBuilder.builder()
                .serverUrl("http://<your-keycloak-server-ip>:<your-keycloak-server-port>/auth")
                .realm("<your-keycloak-realm>")
                .clientId("<your-keycloak-client-id>")
                .clientSecret(keycloakAdminCliSecret)
                .grantType("password")
                .username("<your-keycloak-username>")
                .password("<your-keycloak-password>")
                .build();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail("test1@test1.test");
        userRepresentation.setUsername("MyUser1");
        userRepresentation.setEnabled(true);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue("MyUser1");
        credential.setTemporary(false);
        userRepresentation.setCredentials(Collections.singletonList(credential));
        userRepresentation.setUserProfileMetadata(null);
        Response response = keycloak.realm("neuron").users().create(userRepresentation);
        logger.info(response.getEntity().toString());
        logger.info(response.readEntity(String.class));
        return null;
    }

    @GET
    @Path("/user")
    public String createUser() {
        Response response = keyCloakSettings.registerKeycloakOrgUser("MyUser1", "MyUser1", "MyUser1", "test1@test1.test", "MyUser1", new RealmSensorRole(), keycloakAdminCliSecret);
        logger.info(String.valueOf(response.getStatus()));
        logger.info(response.getStatusInfo().toString());
        logger.info(response.getEntity().toString());
        return response.getEntity().toString();
    }
}
