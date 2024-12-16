package world.neuron.device;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.OffsetDateTime;

@Entity
public class Device extends PanacheEntity {

    public String name;
    public Double latitude;
    public Double longitude;
    public String deviceKey;
    public DeviceRole deviceRole;
    public String deviceType;
    public DeviceStatus deviceStatus;
    public String hederaAccountNumber;
    public String publicKey;
    public String privateKey;
    public String hederaParentAccountNumber;
    public OffsetDateTime registerTime;
    public OffsetDateTime creationTime;
    public OffsetDateTime deletingTime;

    public DeviceRegistrationStatus registrationStatus;
    @Column(name = "account_id")
    public Long accountId;
}
