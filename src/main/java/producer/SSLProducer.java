package producer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.Properties;

public class SSLProducer {

    public static void main(String[] args) {
        String bserver = args[0];
        String secDirLocation = args[1];
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bserver);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        props.put(ProducerConfig.SECURITY_PROVIDERS_CONFIG, "");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, secDirLocation + "kafka.client.truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "clisec");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, secDirLocation + "kafka.client.keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "clisec");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "clisec");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        ProducerRecord<String, String> record = new ProducerRecord<>("sec-topic", "message from a java program to a secure topic");
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.out.println(exception.getMessage());
            } else {
                System.out.println("sec record sent successfully to secure topic");
            }
        });

        producer.flush();
        producer.close();
    }
}
