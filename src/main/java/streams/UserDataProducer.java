package streams;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutionException;



// Run the Kafka Streams application before running the producer.
// This will be best for your learning
public class UserDataProducer {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
        Properties properties = new Properties();

        // kafka bootstrap server
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // producer acks
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all"); // strongest producing guarantee
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "3");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");
        // leverage idempotent producer from Kafka 0.11 !
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // ensure we don't push duplicates
        Producer<String, String> producer = new KafkaProducer<>(properties);


        // FYI - We do .get() to ensure the writes to the topics are sequential, for the sake of the teaching exercise
        // DO NOT DO THIS IN PRODUCTION OR IN ANY PRODUCER. BLOCKING A FUTURE IS BAD!


        // we are going to test different scenarios to illustrate the join

        // 1 - we create a new user, then we send some data to Kafka
        System.out.println("\nExample 1 - new user\n");
        producer.send(userRecord("raman", "First=Raman,Last=Shastri,Email=raman.shastri@gmail.com")).get();
        producer.send(purchaseRecord("raman", "Apples and Bananas (1)")).get();

        Thread.sleep(10000);

        // 2 - we receive user purchase, but it doesn't exist in Kafka
        System.out.println("\nExample 2 - non existing user\n");
        producer.send(purchaseRecord("Shaman", "Forgetting Kafka")).get();

        Thread.sleep(10000);

        // 3 - we update user "raman", and send a new transaction
        System.out.println("\nExample 3 - update to user\n");
        producer.send(userRecord("raman", "First=Ramanna,Last=Shastri,Email=ramanna.shastri@gmail.com")).get();
        producer.send(purchaseRecord("raman", "Oranges (3)")).get();

        Thread.sleep(10000);

        // 4 - we send a user purchase for samar, but it exists in Kafka later
        System.out.println("\nExample 4 - non existing user then user\n");
        producer.send(purchaseRecord("samar", "Computer (4)")).get();
        producer.send(userRecord("samar", "First=Samarkant,Last=Kukreja,GitHub=samarkk")).get();
        producer.send(purchaseRecord("samar", "Books (4)")).get();
        producer.send(userRecord("samar", null)).get(); // delete for cleanup

        Thread.sleep(10000);

        // 5 - we create a user, but it gets deleted before any purchase comes through
        System.out.println("\nExample 5 - user then delete then data\n");
        producer.send(userRecord("praveen", "First=Praveen")).get();
        producer.send(userRecord("praveen", null)).get(); // that's the delete record
        producer.send(purchaseRecord("praveen", "Shrieks (5)")).get();

        Thread.sleep(10000);

        System.out.println("End of demo");
        producer.close();
    }

    private static ProducerRecord<String, String> userRecord(String key, String value){
        return new ProducerRecord<>("usert", key, value);
    }


    private static ProducerRecord<String, String> purchaseRecord(String key, String value){
        return new ProducerRecord<>("userp", key, value);
    }
}
