package streams;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class BankTransactionsProducer {
    public static void main(String[] args) {
        Properties properties = new Properties();

        // kafka bootstrap server
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "master.e4rlearning.com:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // producer acks
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // strongest producing guarantee
//        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,"1");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");
        // leverage idempotent producer from Kafka 0.11 !
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "testtrans");
        // ensure we don't push duplicates

        Producer<String, String> producer = new KafkaProducer<>(properties);
        producer.initTransactions();
        int i = 1;
        while (true) {
            System.out.println("Producing batch: " + i);
            try {
                producer.beginTransaction();
                int pause = Integer.parseInt(args[0]);
                producer.send(newRandomTransaction("ram", i++));
                Thread.sleep(pause);
                producer.send(newRandomTransaction("shyam", i++));
                Thread.sleep(pause);
                producer.send(newRandomTransaction("sohan", i++));
                Thread.sleep(pause);
                producer.send(newRandomTransaction("radha", i++));
                Thread.sleep(pause);
                producer.send(newRandomTransaction("mona", i++));
                Thread.sleep(pause);
                producer.commitTransaction();
            } catch (InterruptedException e) {
                break;
            }
        }
        producer.close();
    }

    public static ProducerRecord<String, String> newRandomTransaction(String name, int amt) {
        // creates an empty json {}
        ObjectNode transaction = JsonNodeFactory.instance.objectNode();

        // { "amount" : 46 } (46 is a random number between 0 and 100 excluded)
        Integer amount = ThreadLocalRandom.current().nextInt(0, 100);

        // Instant.now() is to get the current time using Java 8
        Instant now = Instant.now();

        // we write the data to the json document
        transaction.put("name", name);
        transaction.put("amount", amt);
        transaction.put("time", now.toString());
        return new ProducerRecord<>("bank-transactions", name, transaction.toString());
    }
}
