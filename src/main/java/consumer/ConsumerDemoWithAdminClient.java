package consumer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ConsumerDemoWithAdminClient {
    // create the consumer configurations
    static String bservers = "192.168.181.138:9092";
    static String groupId = "idegr";
    static String topic = "ide_topic";

    private static Properties createConsumerConfiguration(String bservers, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }

    // create a consumer from configurations
    private static KafkaConsumer<String, String> createKafkaConsumer(String bservers, String groupId) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,
                String>(createConsumerConfiguration(bservers, groupId));
        return consumer;
    }
    private  static AdminClient createAdminClient(String bservers){
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        adminProps.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "10000");
        AdminClient adminClient = AdminClient.create(adminProps);
        return adminClient;
    }

    private static Map<TopicPartition, OffsetAndMetadata> getGroupOffsets(String groupId)
            throws ExecutionException, InterruptedException {
        AdminClient adminClient = createAdminClient(bservers);
        Map<TopicPartition, OffsetAndMetadata> groupOffsets =
                adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
        return groupOffsets;
    }

    private static void printGroupOffsets(String groupId)
            throws ExecutionException, InterruptedException {
        Map<TopicPartition, OffsetAndMetadata> currentOffsets =
                getGroupOffsets(groupId);
        for (TopicPartition partition : currentOffsets.keySet()) {
            System.out.println("For topic: " + partition.topic() + ", for partition: " +
                    partition.partition() + ", current offset: "
                    + currentOffsets.get(partition).offset());
        }
    }

    private static void resetGroupOffsets(String bservers, String groupId)
            throws ExecutionException, InterruptedException {
        Map<TopicPartition, OffsetAndMetadata> currentOffsets =
                getGroupOffsets(groupId);
        currentOffsets.keySet().stream().forEach(partition -> {
            System.out.println("For topic: " + partition.topic() + ", for partition: " +
                    partition.partition() + ", current offset: "
                    + currentOffsets.get(partition).offset());
        });
        Map<TopicPartition, OffsetAndMetadata> newOffsets = new HashMap<>();
        currentOffsets.keySet().forEach(ks -> newOffsets.put(ks,
                new OffsetAndMetadata(0)));
        AdminClient adminClient = createAdminClient(bservers);
        adminClient.alterConsumerGroupOffsets(groupId, newOffsets);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // args - 0 - bootstrap server, 1 - group id, 2 - topic
        System.out.println("printing the group offset details");
        printGroupOffsets(args[1]);

        resetGroupOffsets(args[0], args[1]);
        System.out.println("printing the group offset details after resetting to new offsets");
        printGroupOffsets(groupId);
        // subscribe to topic, topics
        KafkaConsumer<String, String> consumer = createKafkaConsumer(args[0], args[1]);
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
//        consumer.close();
//        System.exit(0);
    }
}
