package world.neuron.account;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@RequestScoped
@Path("/api/v1/account")
public class AccountController {

    @Inject
    AccountService accountService;

    @GET
    @Path("/")
    public Account getAccountDetails() {
        return accountService.getCurrentAccount();
    }

    @POST
    @Path("/create")
    public void createNewAccount(Account account) throws Exception {
        accountService.createAccount(account);
    }

}
