package world.neuron.hedera.topic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;


@ApplicationScoped
public class TopicService {

    @Inject
    TopicRepository topicRepository;

    @Transactional
    public Topic createHederaTopics(String accountId, String stdIn, String stdOut, String error) {
        Topic topic = new Topic();
        topic.hederaAccountId = accountId;
        topic.stdIn = stdIn;
        topic.stdOut = stdOut;
        topic.error = error;
        topic.persist();
        return topic;
    }

    @Transactional
    public Topic getTopicByHederaAccountId(String accountId) {
        return topicRepository.find("hederaAccountId", accountId).firstResult();
    }
}
