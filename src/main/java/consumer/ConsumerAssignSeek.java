package consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.Topic;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

public class ConsumerAssignSeek {
    private static Properties createConsumerConfiguration() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.181.138:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "idegr");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }

    // create a consumer from configurations
    private static KafkaConsumer<String, String> createKafkaConsumer() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,
                String>(createConsumerConfiguration());
        return consumer;
    }

    public static void main(String[] args) {
        TopicPartition partitionToReadFrom = new TopicPartition("ide_topic", 0);
        long offsetToReadFrom = 5L;
        KafkaConsumer<String, String> consumer = createKafkaConsumer();
        consumer.assign(Arrays.asList(partitionToReadFrom));
        consumer.seek(partitionToReadFrom, offsetToReadFrom);
        int batchNo = 0;
        int counter = 0;
        while (batchNo < 5) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
            batchNo++;
            System.out.println("No of records gotten in batch " + batchNo + ": " + records.count());
            for (ConsumerRecord record : records) {
                counter++;
                if (counter % 250 == 0)
                    System.out.println("Topic: " + record.topic() + ", Partition: " + record.partition() + ", Offset: " + record.offset() + ", Value: " + record.value());
            }
            counter = 0;
        }
    }
}
