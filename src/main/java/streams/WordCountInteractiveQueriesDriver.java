package streams;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
public class WordCountInteractiveQueriesDriver {

    public static void main(final String [] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : "skkvm.eastus.cloudapp.azure.com:9092";
        final List<String> inputValues = Arrays.asList("hello world",
                "all streams lead to kafka",
                "streams",
                "kafka streams",
                "the cat in the hat",
                "green eggs and ham",
                "that sam i am",
                "up the creek without a paddle",
                "run forest run",
                "a tank full of gas",
                "eat sleep rave repeat",
                "one jolly sailor",
                "king of the world");

        final Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        final KafkaProducer<String, String>
                producer =
                new KafkaProducer<>(producerConfig, new StringSerializer(), new StringSerializer());

        // Always begin with writing the first line to the topic so that the REST API example calls in
        // our step-by-step instructions always work right from the start (otherwise users may run into
        // HTTP 404 errors when querying the latest value for a key, for example, until the right input
        // data was sent to the topic).
        producer.send(new ProducerRecord<>(WordCountInteractiveQueriesExample.TEXT_LINES_TOPIC,
                inputValues.get(0), inputValues.get(0)));

        // every 500 milliseconds produce one of the lines of text from inputValues to the
        // TextLinesTopic
        final Random random = new Random();
        while (true) {
            final int i = random.nextInt(inputValues.size());
            producer.send(new ProducerRecord<>(WordCountInteractiveQueriesExample.TEXT_LINES_TOPIC,
                    inputValues.get(i), inputValues.get(i)));
            Thread.sleep(500L);
        }
    }

}
