package world.neuron.keycloak.auth;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RealmUser {
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public boolean enabled;
    public List<Map<String,String>> credentials;

    public List<Map<String,String>> toCredentials (String password){
        return Arrays.asList(Map.of("type", "password", "value", password, "temporary", "false"));
    }
}
