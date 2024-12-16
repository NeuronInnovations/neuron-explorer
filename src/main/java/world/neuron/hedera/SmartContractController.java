package world.neuron.hedera;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@RequestScoped
@Path("/contract")
public class SmartContractController {


    @Inject
    SmartContractService contractService;
    @GET
    public String getContract() throws Exception {
        contractService.readSmartContract();
        return null;
    }
}
