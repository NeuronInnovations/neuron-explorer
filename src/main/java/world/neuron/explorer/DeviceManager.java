package world.neuron.explorer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import world.neuron.device.Device;
import world.neuron.device.DeviceService;

import java.util.List;

@Path("/devicemanager")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceManager {

    @Inject
    Template devicemanager;

    @Inject
    DeviceService deviceService;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        List<String> roles = securityIdentity.getRoles().stream().toList();

        List<Device> devices = deviceService.getActiveDevicesForCurrentAccount();

        return devicemanager.instance()
                .data("pagename", "Device manager")
                .data("devices", devices)
                .data("role", roles.contains("user") ? "user" : null)
                .data("owner", securityIdentity.getPrincipal().getName());
    }
}
