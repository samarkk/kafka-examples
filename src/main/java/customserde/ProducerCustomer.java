import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerCustomer {
    public static void main(String[] args) {
        String broker = "127.0.0.1:9092";
        String topic = "customer";

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomerSerializer.class.getName());

        KafkaProducer<String, Customer> producer = new KafkaProducer<String, Customer>(props);

        for(int n = 1; n <= 10; n++){
            Customer customer = new Customer(n, "Customer " + n);
            ProducerRecord record = new ProducerRecord(topic, "id " + n, customer);
            producer.send(record);
        }

        producer.flush();
        producer.close();

    }
}
