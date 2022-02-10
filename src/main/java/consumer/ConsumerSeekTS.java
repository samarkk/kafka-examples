package consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConsumerSeekTS {
    private static Properties createConsumerConfiguration(String bservers, String group) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        return props;
    }

    // create a consumer from configurations
    private static KafkaConsumer<String, String> createKafkaConsumer(String bservers, String group) {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,
                String>(createConsumerConfiguration(bservers, group));
        return consumer;
    }

    private static Map<TopicPartition, Long> timestamps = new HashMap<>();

    public static void main(String[] args) {
//        Date currentDate = new java.sql.Date();
        long tstToSearchFrom = Instant.now().toEpochMilli() - 10 * 60 * 1000;
        long tsToSearchTo = Instant.now().toEpochMilli();
        KafkaConsumer<String, String> consumer =
                ConsumerSeekTS.createKafkaConsumer("master:9092", "tsgroup");
        boolean offsetFound = false;
        System.out.println(tstToSearchFrom + ", " + tsToSearchTo);
        for (long ts = tstToSearchFrom; ts < tsToSearchTo; ts++) {
            timestamps.put(new TopicPartition("ide_topic", 5), ts);
            Map<TopicPartition, OffsetAndTimestamp> offsets =
                    consumer.offsetsForTimes(timestamps);
            OffsetAndTimestamp oset = offsets.get(new TopicPartition("ide_topic", 5));
            if (oset != null) {
                offsetFound = true;
                System.out.println("found offset: " + oset.offset());
                break;
            } else {
                if (ts % 20000 == 0) {
                    System.out.println("now at timestamp: " + ts);
                    System.out.println("could not find offset");
                }
            }

//            offsets.forEach((tp, oss) -> {
//                if(oss != null) {
//                    System.out.println(tp + "," + oss.offset());
//                } else
//                    System.out.println(tp);
//            });

        }
    }

}
