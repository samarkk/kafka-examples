package consumer;

import com.example.Customer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import java.util.Collections;
import java.util.Properties;

public class AvroConsumer {
    public static void main(String[] args) {
        String bservers = "192.168.50.2:9092";
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", "http://192.168.50.2:8081");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cust-avro-gr");
        props.put("specific.avro.reader", true);
        KafkaConsumer<String, Customer> customerKafkaConsumer = new KafkaConsumer<String, Customer>(props);
        customerKafkaConsumer.subscribe(Collections.singleton("customer-avro"));

        System.out.println("Waiting for data...");

        while (true) {
            System.out.println("Polling");
            ConsumerRecords<String, Customer> records = customerKafkaConsumer.poll(1000);

            for (ConsumerRecord<String, Customer> record : records) {
                Customer customer = record.value();
                System.out.println(customer);
            }

            customerKafkaConsumer.commitSync();
        }

    }
}
