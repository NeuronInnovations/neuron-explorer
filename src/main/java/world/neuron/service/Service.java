package world.neuron.service;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Service extends PanacheEntity {
    public ServiceName name;
    public ServiceType type;
    public String documentUrl;

}
