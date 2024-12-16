package world.neuron.serviceregister;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServiceRegisterRepository implements PanacheRepository<ServiceRegister> {
}
