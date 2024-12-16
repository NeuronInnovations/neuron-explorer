package world.neuron.keycloak.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import java.util.Collections;
import java.util.HashMap;

@ApplicationScoped
public class KeyCloakSettings {
    @ConfigProperty(name = "auth.host")
    String host;
    @ConfigProperty(name = "auth.protocol")
    String protocol;
    @ConfigProperty(name = "auth.port")
    String port;


    private final String AUTH_PATH = String.format("%s://%s:%s", protocol, host, port);


    private final Client client;

    public KeyCloakSettings() {
        this.client = ResteasyClientBuilder.newClient();
    }


    public Form getForm(String clientSecret, String clientId) {
        return new Form()
                .param("grant_type", "client_credentials")
                .param("client_secret", clientSecret)
                .param("client_id", clientId);
    }

    public WebTarget getCredentials() {

        return client.target(AUTH_PATH + "/admin/realms/<name-of-your-realm>/protocol/openid-connect/token/");
    }

    public Response credPostResponse(WebTarget credentials, Form request) {
        return credentials.request(MediaType.APPLICATION_FORM_URLENCODED).post(Entity.form(request));
    }

    public String getAccessToken(Response response) {
        if (response.getEntity() != null) {
            String entity = response.getEntity().toString();
            entity.toString();
        }
        HashMap token = response.readEntity(HashMap.class);
        return token.get("access_token").toString();

    }

    public Response postWithAccessToken(String accessToken, WebTarget webTarget, Entity<?> entity) {
        Response post = webTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .post(entity);
        return post;
    }

    public Response getCredentialResponse(String clientSecret, String clientId) {
        Form adminRequest = getForm(clientSecret, clientId);

        WebTarget admincreds = getCredentials();
        return credPostResponse(admincreds, adminRequest);
    }

    public Response registerKeycloakOrgUser(
            String username,
            String firstname,
            String lastname,
            String email,
            String password,
            NeuronRoleInterface role,
            String keyCloackAdminCliSecret) {
        Response admincredsResponse = getCredentialResponse(keyCloackAdminCliSecret, "<your-keycloak-client-id>");

        if (admincredsResponse.getStatus() != Response.Status.OK.getStatusCode()) return admincredsResponse;

        String adminAccessToken = getAccessToken(admincredsResponse);

        WebTarget useradd = ResteasyClientBuilder.newClient()
                .target(AUTH_PATH + "/admin/realms/<name-of-your-realm>/users");
        RealmUser realmUser = getRealmOrgUser(username, firstname, lastname, email, password);
        Response useraddresponse = postWithAccessToken(adminAccessToken, useradd, Entity.json(realmUser));

        if (useraddresponse.getStatus() != Response.Status.CREATED.getStatusCode()) return useraddresponse;
        String location = useraddresponse.getHeaderString("location");
        String id = location.substring(location.lastIndexOf('/') + 1);

        WebTarget userRoleMappings = client.target(AUTH_PATH + "/admin/realms/<name-of-your-realm>/users/" + id + "/role-mappings/realm");

        Response roleAddResponse = postWithAccessToken(adminAccessToken, userRoleMappings, Entity.json(Collections.singletonList(role)));

        if (roleAddResponse.getStatus() != Response.Status.NO_CONTENT.getStatusCode()) {
            return roleAddResponse;
        }

        return Response.ok().build();
    }

    private RealmUser getRealmOrgUser(String username, String firstname, String lastname, String email, String password) {
        RealmUser realmUser = new RealmUser();
        realmUser.username = username;
        realmUser.email = email;
        realmUser.firstName = firstname;
        realmUser.lastName = lastname;
        realmUser.enabled = true;
        realmUser.credentials = realmUser.toCredentials(password);
        return realmUser;
    }
}
