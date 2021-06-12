package consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ConsumerDemo {
    // create the consumer configurations

    private static Properties createConsumerConfiguration(String bservers, String groupId, String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        // Group id has to be provided
        //Exception in thread "main" org.apache.kafka.common.errors.InvalidGroupIdException: To use the group management or offset commit APIs, you must provide a valid group.id in the consumer configuration
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }

    // create a consumer from configurations
    private static KafkaConsumer<String, String> createKafkaConsumer(String bservers, String groupId, String topic) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,
                String>(createConsumerConfiguration(bservers, groupId, topic));
        return consumer;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // subscribe to topic, topics
        KafkaConsumer<String, String> consumer = createKafkaConsumer(args[0], args[1], args[2]);
        consumer.subscribe(Collections.singleton(args[2]));
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                if (records.count() > 0) {
                    for (ConsumerRecord<String, String> record : records) {
                        String recordVal = record.value().toUpperCase();
                        System.out.println("Record partition: " + record.partition() +
                                ", offset: " + record.offset() + ", value " +
                                "processed: " + recordVal);
                    }
                    consumer.commitSync();
                }
            }
        } catch (Exception ex) {
            System.out.println("Some exception happened");
            ex.printStackTrace();
        } finally {
            consumer.close();
        }
    }
}
