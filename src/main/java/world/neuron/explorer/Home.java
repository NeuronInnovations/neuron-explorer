package world.neuron.explorer;

import io.quarkus.oidc.IdToken;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Multi;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.SseElementType;
import world.neuron.hedera.DeviceInfo;

import java.util.List;

@Path("")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class Home {

    @Inject
    Template home;

    @Inject
    @IdToken
    JsonWebToken idToken;

    @Inject
    JsonWebToken jwt;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    HomeService homeService;

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance renderHome() {
        List<String> roles = this.securityIdentity.getRoles().stream().toList();
        return home.instance()
                .data("pagename", "home")
                .data("businessHandle", this.idToken.getClaim("preferred_username"))
                .data("role", roles.contains("user") ? "user" : null)
                .data("email", this.idToken.getClaim("email"))
                .data("firstName", this.jwt.getClaim("given_name"))
                .data("lastName", this.jwt.getClaim("family_name"));
    }

    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.APPLICATION_JSON)
    @GET
    @Path("emitDevices")
    public Multi<List<DeviceInfo>> emitDevices() {
        return homeService.emitDevices();
    }

    @GET
    @Path("getStats")
    public JsonObject getStats() {
        return homeService.getStats();
    }
}
