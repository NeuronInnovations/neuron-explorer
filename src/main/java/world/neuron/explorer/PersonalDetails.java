package world.neuron.explorer;

import java.util.Map;
import java.util.TreeMap;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/personaldetails")
@SessionScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonalDetails {

    @Inject
    Template personaldetails;

    @Inject
    SecurityIdentity securityIdentity;

    private Map<String, Integer> getCountries() {

        return new TreeMap<>(Map.ofEntries(
                Map.entry("Germany", 276),
                Map.entry("Greece", 300),
                Map.entry("Hungary", 348),
                Map.entry("United States of America", 840),
                Map.entry("United Kingdom of Great Britain and Northern Ireland", 826)));
    }

    @GET
    @Consumes(MediaType.TEXT_HTML)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("referer") String referer) {
        return personaldetails.instance()
                .data("pagename", "Personal Details")
                .data("countries", this.getCountries())
                .data("user", null)
                .data("owner", securityIdentity.getPrincipal().getName());
    }
}
