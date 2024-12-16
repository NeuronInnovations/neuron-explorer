package world.neuron.explorer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import world.neuron.account.Account;
import world.neuron.account.AccountService;

import java.util.List;

@Path("/accountdetails")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountDetails {

    @Inject
    Template accountdetails;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    AccountService accountService;

    @Inject
    JsonWebToken jsonWebToken;

    @GET
    @RolesAllowed({"user"})
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() throws Exception {
        List<String> roles = securityIdentity.getRoles().stream().toList();

        Account account = accountService.getCurrentAccount();
        if (account == null) {
            account = new Account();
            account.email = jsonWebToken.claim("email").get().toString();
            account.firstName = jsonWebToken.claim("given_name").get().toString();
            account.lastName = jsonWebToken.claim("family_name").get().toString();
            accountService.generateECDSAandPersist(account);
        }

        return accountdetails.instance()
                .data("pagename", "Account Details")
                .data("role", roles.contains("user") ? "user" : null)
                .data("account", account)
                .data("owner", securityIdentity.getPrincipal().getName());
    }
}
