package streams.statelessprocessing;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.kstream.Produced;
import schemalessreg.AvroDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class CryptoDesConsumer {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "master:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, AvroDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "crypgr");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, AvroDeserializer> consumer = new KafkaConsumer<String, AvroDeserializer>(props);
        consumer.subscribe(Collections.singletonList("crypto-sentiment"));
        int polls = 0;
        while (true) {
            ConsumerRecords<String, AvroDeserializer> records = consumer.poll(Duration.ofMillis(5000));
            polls++;
            if (records.count() > 0) {
                for (ConsumerRecord<String, AvroDeserializer> record : records) {
                    System.out.println(record);
                }
            }
            if (polls % 100 == 0) {
                System.out.println("100 records polled");
            }
        }
    }

}
