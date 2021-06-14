package producer;

import com.example.Customer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class AvroProducer {
    public static void main(String[] args) {
        // args - 0 -bootstrap servers, 1 - schema registry, 2 - topic
        Properties properties = new Properties();
        // normal producer
        properties.setProperty("bootstrap.servers", args[0]);
        properties.setProperty("acks", "all");
        properties.setProperty("retries", "10");
        // avro part
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", KafkaAvroSerializer.class.getName());
        properties.setProperty("schema.registry.url", args[1]);


        Producer<String, Customer> producer = new KafkaProducer<String, Customer>(properties);

        String topic = args[2];

        // use the version 1 schema
        Customer customer = Customer.newBuilder()
                .setAge(34)
                .setAutomatedEmail(false)
                .setFirstName("Waman")
                .setLastName("Shastri")
                .setHeight(177f)
                .setWeight(73f)
                .build();

        // use the version 2 schema
//        Customer customer = Customer.newBuilder()
//                .setAge(34)
//                .setFirstName("Chaman")
//                .setLastName("Shastri")
//                .setHeight(173f)
//                .setWeight(75f)
//                .build();


        ProducerRecord<String, Customer> producerRecord = new ProducerRecord<String, Customer>(
                topic, customer
        );

        System.out.println(customer);
        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception == null) {
                    System.out.println(metadata);
                } else {
                    exception.printStackTrace();
                }
            }
        });

        producer.flush();
        producer.close();

    }
}
