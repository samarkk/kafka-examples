package streams.statelessprocessing;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import streams.statelessprocessing.language.DummyClient;
import streams.statelessprocessing.language.LanguageClient;

import java.util.Arrays;
import java.util.Properties;

public class TwitterSentimentApp {
    public static void main(String[] args) {

        CryptoTopology.terms = Arrays.asList("kotlin","groovy");
//        CryptoTopology.schemaRegistryURL = "http://master.e4rlearning.com:7788/api/v1";
        LanguageClient languageClient = new DummyClient();
        Topology topology = CryptoTopology.build(languageClient);
        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "stlapp");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "master:9092");

        KafkaStreams streams = new KafkaStreams(topology, config);

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        System.out.println("Starting Twitter streams");
        streams.start();
    }

}
