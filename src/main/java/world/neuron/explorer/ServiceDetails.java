package world.neuron.explorer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import world.neuron.serviceregister.ServiceRegister;
import world.neuron.serviceregister.ServiceRegisterUtility;

import java.util.List;

@Path("/servicedetails")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceDetails {

    @Inject
    Template servicedetails;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    ServiceRegisterUtility registerUtility;

    @Path("{id:\\d+}/{deviceName}")
    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@PathParam("id") Long id, @PathParam("deviceName") String deviceName) {

        List<String> roles = this.securityIdentity.getRoles().stream().toList();

        ServiceRegister service = registerUtility.findRegisterById(id);

        return servicedetails.instance()
                .data("pagename", "Service Details")
                .data("role", roles.contains("user") ? "user" : null)
                .data("deviceName", deviceName)
                .data("service", service)
                .data("owner", securityIdentity.getPrincipal().getName());
    }
}
