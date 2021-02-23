package streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class WordCountWindowsApp {
    public static void main(String[] args) {
        WordCountWindowsApp wordCountWindowsApp = new WordCountWindowsApp();
        wordCountWindowsApp.createAndRunWindowWordCountTable(args[0], args[1], args[2], args[3]);
    }

    // create properties
    private Properties createStreamConfigs(String bservers) {
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "wcapp-win");
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                Serdes.String().getClass().getName());
        properties.setProperty(StreamsConfig.POLL_MS_CONFIG, "100");
        properties.setProperty(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, "100");
        return properties;
    }

    // create  a stream builder
    // build a topology
    private void createAndRunWindowWordCountTable(String bservers, String topic, String windur, String slidedur) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream(topic);

        KTable<Windowed<String>, Long> windowTable =
                textLines.mapValues(line -> line.toLowerCase()).
                        flatMapValues(line -> Arrays.asList(line.split("\\W+")))
                        .selectKey((key, word) -> word)
                        .groupByKey()
                        .windowedBy(TimeWindows.of(Duration.ofSeconds(Long.parseLong(windur)))
                                .advanceBy(Duration.ofSeconds(Long.parseLong(slidedur))))
                        .count();

        // create stream by building the topology with properties using the stream builder
        // run stream
        windowTable.toStream().map(
                (key, count) ->
                        new KeyValue<>(key.toString().substring(1, key.toString().indexOf("@")) + " " +
                                key.window().startTime() + "->"
                                + key.window().endTime(),
                                count.toString())).to("wco-tw", Produced.with(
                Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), createStreamConfigs(bservers));
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
