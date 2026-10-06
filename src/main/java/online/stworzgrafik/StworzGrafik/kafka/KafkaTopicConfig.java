package online.stworzgrafik.StworzGrafik.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Value("${app.kafka.demo.partitions}")
    private int partitions;

    @Value("${app.kafka.demo.replication-factor}")
    private int replicationFactor;

    @Bean
    public NewTopic demoCreatedTopic(){  return createTopic(KafkaTopics.DEMO_CREATED);}

    @Bean
    public NewTopic demoExpiredTopic(){
        return createTopic(KafkaTopics.DEMO_EXPIRED);
    }

    @Bean
    public NewTopic demoDeletedTopic(){
        return createTopic(KafkaTopics.DEMO_DELETED);
    }

    private NewTopic createTopic(KafkaTopics topics){
        return new NewTopic(topics.name(), partitions,(short) replicationFactor);
    }
}
