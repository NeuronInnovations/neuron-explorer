package world.neuron.explorer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/selectserviceparameters")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SelectServiceParameters {

    @Inject
    Template selectserviceparameters;

    @Inject
    SecurityIdentity securityIdentity;

    @Path("{id:\\d+}")
    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@PathParam("id") Integer serviceId) {

        return selectserviceparameters.instance()
                .data("pagename", "Select Service Parameters")
                .data("owner", securityIdentity.getPrincipal().getName());
    }
}
