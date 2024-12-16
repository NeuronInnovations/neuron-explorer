package world.neuron.explorer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import world.neuron.service.Service;
import world.neuron.service.ServiceUtility;

import java.util.List;

@Path("/linkserviceagreement")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinkServiceAgreement {

    @Inject
    Template linkserviceagreement;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    ServiceUtility serviceUtility;

    @Path("{name}")
    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@PathParam("name") String name) {

        List<String> roles = securityIdentity.getRoles().stream().toList();
        List<Service> serviceList = serviceUtility.getUnassignedServices(name);

        return linkserviceagreement.instance()
                .data("pagename", "Link Service Agreement")
                .data("deviceName", name)
                .data("role", roles.contains("user") ? "user" : null)
                .data("services", serviceList)
                .data("owner", securityIdentity.getPrincipal().getName());
    }
}
