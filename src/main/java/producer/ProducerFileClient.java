package producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ProducerFileClient {

    public static void main(String[] args) throws IOException {
// args - 0 - bootstrap server, 1 - topic, 2 - file to read from
        // create properties for producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, args[0]);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
//        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "15000");
//        props.put(ProducerConfig.)
//        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
//        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "130000");
        // create producer using properties
        KafkaProducer<String, String> producer = new KafkaProducer<String,
                String>(props);

        FileReader fr = new FileReader(args[2]);
        BufferedReader br = new BufferedReader(fr);
        br.readLine();
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(args[1], line);
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        String sendDetails =
                                "Producer Record sent to topic: " + metadata.topic() +
                                        "\nPartition: " + metadata.partition() +
                                        "\nKey: " + record.key() +
                                        "\nOffset: " + metadata.offset() +
                                        "\nTimestamp: " + metadata.timestamp();
                        System.out.println(sendDetails);
                    }
                });
            }
            producer.flush();
        } catch (Exception ex) {
            System.out.println("Exception underway");
            ex.printStackTrace();
        } finally {
            System.out.println("Finally block in action");
            producer.close();
        }
    }
}
