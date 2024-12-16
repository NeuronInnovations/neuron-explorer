package world.neuron.serviceregister;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import world.neuron.service.Service;

@RequestScoped
@Path("/api/v1/service/register")
public class ServiceRegisterController {

    @Inject
    ServiceRegisterUtility serviceRegisterUtility;

    @POST
    public Service registerService(ServiceRegister register) {
        serviceRegisterUtility.registerService(register);
        return null;
    }
}
