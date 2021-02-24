package consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;

public class ConsumerDemoWRBL {
    // create consumer configs
    private static Properties createConsumerConfiguration() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "skkvm.eastus.cloudapp.azure.com:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "idegr");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }

    // create consumer
    // create a consumer from configurations
    private static KafkaConsumer<String, String> createKafkaConsumer() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,
                String>(createConsumerConfiguration());
        return consumer;
    }

    public static void main(String[] args) {
        // subscribe to topic, add a rebalance listener
        KafkaConsumer<String, String> consumer = createKafkaConsumer();
        ReblanceListener reblanceListener = new ReblanceListener(consumer);
        consumer.subscribe(Collections.singleton("idetopic2"), reblanceListener);
        // poll
        while(true){
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100));
            if (records.count() > 0) {
                for (ConsumerRecord<String, String> record : records) {
                    String recordVal = record.value().toUpperCase();
                    System.out.println("Record partition: " + record.partition() +
                            ", offset: " + record.offset() + ", value " +
                            "processed: " + recordVal);
                    reblanceListener.addOffset("idetopic2", record.partition(), record.offset());
                }
//                consumer.commitSync();
            }
        }
    }
}

class ReblanceListener implements ConsumerRebalanceListener {
    private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private KafkaConsumer<String, String> consumer;

    public ReblanceListener(KafkaConsumer<String, String> consumer) {
        this.consumer = consumer;
    }

    public void addOffset(String topic, int partition, long offset){
        currentOffsets.put(new TopicPartition(topic, partition), new OffsetAndMetadata(offset));
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        System.out.println("Partition revoked ....");
        System.out.println("About to make the following commits");
//        partitions.stream().forEach(part -> {
//            System.out.println("Partition: " + part.partition());
//        });
        currentOffsets.keySet().stream().forEach(tpart -> {
            System.out.println("Partition: " + tpart.partition() + ", offset: " + currentOffsets.get(tpart).offset());
        });
        consumer.commitSync(currentOffsets);
        currentOffsets.clear();
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        System.out.println("Patitions assigned with details below: ");
        for(TopicPartition partition: partitions){
            System.out.println("Partition: " + partition.partition());
        }
    }
}
