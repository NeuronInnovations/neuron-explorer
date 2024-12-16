package world.neuron.account;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;


@Entity
public class Account extends PanacheEntity {

    public String firstName;
    public String lastName;
    public String location;
    public String hederaAccountNumber; // evmAddress
    public String hederaEvmAddress;
    public String publicKey;
    public String privateKey;
    public String email;
}
