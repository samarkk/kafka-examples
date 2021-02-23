package streams;

import com.example.Nseforec;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
//import io.confluent.kafka

import java.util.Properties;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;

public class SplitStreamNSEFO {
    static String bservers = "52.234.178.74:9092";
    static String schemaRegistryURL = "http://52.234.178.74:8081";
    static String inputTopic = "nsefotopic_avro";
    static String gt1mtopic = "gr-1mn";
    static String _1lto1mtopic = "lakh-to-mn";
    static String lt1ltopic = "upto1lakh";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryURL);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "nsefo-brranch");
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Nseforec>[] branches = builder.<String, Nseforec>stream(inputTopic)
                .branch(
                        (key, value) -> value.getOpenint() > 1000000,
                        (key, value) -> value.getOpenint() > 100000 && value.getOpenint() < 1000000,
                        (key, value) -> true
                );
        branches[0].to(gt1mtopic);
        branches[1].to(_1lto1mtopic);
        branches[2].to(lt1ltopic);

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
