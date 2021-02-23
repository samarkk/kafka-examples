package streams;

import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.StreamJoined;

import java.time.Duration;
import java.util.Properties;

public class StreamStreamJoinDemo {

    public static void main(String[] args) {
        String bservers = args.length > 0 ? args[0] : "52.234.178.74:9092";
        // create streams configuration properties
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ssjapp");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.STATE_DIR_CONFIG, "/home/azureuser/kstapp");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> alerts = builder.stream("impressions");
        KStream<String, String> incidents = builder.stream("clicks");

        final KStream<String, String> impressionsAndClicks = alerts.outerJoin(
                incidents, (impv, clickv) ->
                        (clickv == null) ? impv + "/not-clicked-yet": impv + "/" + clickv,
                JoinWindows.of(Duration.ofSeconds(Integer.parseInt(args[1]))),
                StreamJoined.with(
                        Serdes.String(), Serdes.String(), Serdes.String()
                )
        );
        impressionsAndClicks.to("impandclicks");
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> streams.close()));
//        while (true) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException iex) {
//                iex.printStackTrace();
//            }
//        }

    }
}
