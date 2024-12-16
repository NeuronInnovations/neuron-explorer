package world.neuron.hedera.topic;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Topic extends PanacheEntity {

    public String hederaAccountId;
    public String stdIn;
    public String stdOut;
    public String error;
}
