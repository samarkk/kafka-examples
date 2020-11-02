package streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.tinkerpop.gremlin.structure.T;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class WordCountApp {
    public static void main(String[] args) {
        // create streams configuration properties
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wcnew");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Streams builder stream from a topic
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream("wci");

        // build a topology
        KTable<String, Long> wordCounts = textLines
                .mapValues(line -> line.toLowerCase())
                .flatMapValues(line -> Arrays.asList(line.split("\\W+")))
                .selectKey((key, word) -> word)
                .groupByKey()
                .count();

        // output the stream
        wordCounts.toStream().to("wco", Produced.with(Serdes.String(), Serdes.Long()));

        KTable<Windowed<String>, Long> wordCountsWindows = textLines
                .mapValues(line -> line.toLowerCase())
                .flatMapValues(line -> Arrays.asList(line.split("\\W+")))
                .selectKey((key, word) -> word)
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(5)).advanceBy(Duration.ofSeconds(2)))
                .count();

        wordCountsWindows.toStream()
                .map((key, wcounts) -> new KeyValue<>(key.toString() + "  " +
                        key.window().startTime() + "-" +
                        key.window().endTime(), wcounts))
                .to("wcotw", Produced.with(Serdes.String(), Serdes.Long()));

        // create streams fro the builder and start it
        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> streams.close()));
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
        }
    }
}
