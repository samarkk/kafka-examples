package custompartitioner;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class CustomPartitionerProducerDemo {
    public static void main(String[] args) {
        // create properties for producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, args[0]);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
        // create producer using properties
        KafkaProducer<String, String> producer = new KafkaProducer<String,
                String>(props);

        // check for different keys as per the partitioner created
        for (int n = 0; n < 100; n++) {
            producer.send(new ProducerRecord<>("cust_part_topic", "abc",
                    "hello to custom partition topic from key abc"));
            // send the records to the broker
            producer.send(new ProducerRecord<>("cust_part_topic", "ijk",
                    "hello to custom partition topic from key ijk"));
            producer.send(new ProducerRecord<>("cust_part_topic", "sjk",
                    "hello to custom partition topic from key sjk"));
        }
        producer.flush();
    }
}