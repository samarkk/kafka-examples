package streams.statelessprocessing;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class CryptoDesConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "master:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "crypgr");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, KafkaAvroDeserializer> consumer = new KafkaConsumer<String, KafkaAvroDeserializer>(props);
        consumer.subscribe(Collections.singletonList("crypto-sentiment"));
        int polls = 0;
        while (true) {
            ConsumerRecords<String, KafkaAvroDeserializer> records = consumer.poll(Duration.ofMillis(5000));
            polls++;
            if (records.count() > 0) {
                for (ConsumerRecord<String, KafkaAvroDeserializer> record : records) {
                    System.out.println(record);
                }
            }
            if (polls % 100 == 0) {
                System.out.println("100 records polled");
            }
        }
    }

}
