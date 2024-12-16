package world.neuron.explorer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.neuron.device.Device;
import world.neuron.device.DeviceService;
import world.neuron.serviceregister.ServiceRegister;
import world.neuron.serviceregister.ServiceRegisterUtility;

import java.util.List;

@Path("/devicedetails")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceDetails {

    private static Logger logger = LoggerFactory.getLogger(DeviceDetails.class);

    @Inject
    Template devicedetails;

    @Inject
    DeviceService deviceService;

    @Inject
    ServiceRegisterUtility registerUtility;

    @Inject
    SecurityIdentity securityIdentity;

    @Path("{name}")
    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@PathParam("name") String name) {

        List<String> roles = securityIdentity.getRoles().stream().toList();
        Device device = this.deviceService.getDeviceByNameForCurrentAccount(name);
        List<ServiceRegister> serviceRegisterList = registerUtility.findRegistersByDeviceName(name);
        return devicedetails.instance()
                .data("pagename", "Device Details")
                .data("device", device)
                .data("services", serviceRegisterList)
                .data("role", roles.contains("user") ? "user" : null)
                .data("owner", securityIdentity.getPrincipal().getName());
    }
    
    @Path("/registerfinal/{name}")
    public void registerDeviceFinal(@PathParam("name") String deviceName) {
        logger.info("finzlizing");
        deviceService.registerDeviceFinal(deviceName);
        logger.info("finalized : "+ deviceName );
    }

    @Path("/delete/{name}")
    @DELETE
    public void deleteDevice(@PathParam("name") String name) {
        deviceService.deleteDevice(name);
    }
}
