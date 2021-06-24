package streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

public class StreamTableJoinDemo {

    public static void main(String[] args) {
        String bservers = args[0];
        String appName = args[1];
        String userRegionTopic = "user-regions";
        String userClicksTopic = "user-clicks";
        String outputTopic = "user-clicks-region";
        // create prperties
        Properties streamProps = new Properties();
        streamProps.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
        streamProps.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        streamProps.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        streamProps.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        // create topology
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, Long> userClicksStream = builder.stream(userClicksTopic,
                Consumed.with(Serdes.String(), Serdes.Long()));
        KTable<String, String> userRegionTable = builder.table(userRegionTopic);
        final KTable<String, Long> clicksPerRegion =
                userClicksStream.leftJoin(userRegionTable, (clicks, region) -> new RegionWithClicks(
                        region == null ? "UNKNOWN" : region, clicks))
                        .map((user, regionClicks) -> new KeyValue<>(regionClicks.getRegion(),
                                regionClicks.getClicks()))
                        .groupByKey(Grouped.with(Serdes.String(), Serdes.Long()))
                        .reduce((a, b) -> a + b);
        // create streams
        clicksPerRegion.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));
        KafkaStreams streams = new KafkaStreams(builder.build(), streamProps);
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

        // start streams
    }

    private static final class RegionWithClicks {

        private final String region;
        private final long clicks;

        RegionWithClicks(final String region, final long clicks) {
            if (region == null || region.isEmpty()) {
                throw new IllegalArgumentException("region must be set");
            }
            if (clicks < 0) {
                throw new IllegalArgumentException("clicks must not be negative");
            }
            this.region = region;
            this.clicks = clicks;
        }

        String getRegion() {
            return region;
        }

        long getClicks() {
            return clicks;
        }

    }

}
