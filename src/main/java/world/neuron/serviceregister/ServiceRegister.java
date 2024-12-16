package world.neuron.serviceregister;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import world.neuron.device.Device;
import world.neuron.service.Service;

import java.math.BigDecimal;

@Entity
public class ServiceRegister extends PanacheEntity {

    public BigDecimal fee;
    public ServiceStatus status;

    public ServiceState serviceState;

    @Column(name = "device_id")
    public Long deviceId;

    @Column(name = "service_id")
    public Long serviceId;

    @Transient
    public Device device;

    @Transient
    public Service service;
}
