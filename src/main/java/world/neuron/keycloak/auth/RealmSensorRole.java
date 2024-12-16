package world.neuron.keycloak.auth;

public class RealmSensorRole implements NeuronRoleInterface{
    public boolean clientRole = false;
    public boolean composite = false;
    public String containerId = "neuron";
    public String id = "<your-client-id>";
    public String name = "sensor";
}
