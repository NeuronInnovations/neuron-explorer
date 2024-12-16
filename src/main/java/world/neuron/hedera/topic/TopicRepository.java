package world.neuron.hedera.topic;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TopicRepository implements PanacheRepository<Topic> {
}
