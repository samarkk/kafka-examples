package streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class WordCountAppStoreDemo {
    static String bservers = "skkvm.eastus.cloudapp.azure.com:9092";

    public static void main(String[] args) {
        // create streams configuration properties
        String wcStoreName = "wc-store";
        String twStoreName = "tw-store";
        String swStoreName = "sw-store";
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wcapp");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//        props.put(StreamsConfig.STATE_DIR_CONFIG, "/home/azureuser/kstapp");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Streams builder stream from a topic
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream("wci");

        // build a topology
        KTable<String, Long> wordCounts = textLines
                .mapValues(line -> line.toLowerCase())
                .flatMapValues(line -> Arrays.asList(line.split(" ")))
                .selectKey((key, word) -> word)
                .groupByKey()
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(wcStoreName));

        // output the stream
        wordCounts.toStream().to("wco", Produced.with(Serdes.String(), Serdes.Long()));

        KTable<Windowed<String>, Long> wordCountTumblingWindows = textLines
                .mapValues(line -> line.toLowerCase())
                .flatMapValues(line -> Arrays.asList(line.split("\\W+")))
                .selectKey((key, word) -> word)
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(5)))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(twStoreName));

        wordCountTumblingWindows.toStream()
                .map((key, wcounts) -> new KeyValue<>(key.toString().substring(
                        1, key.toString().indexOf("@")
                ) + "  " +
                        key.window().startTime() + "->" +
                        key.window().endTime(), wcounts))
                .to("wco-tw", Produced.with(Serdes.String(), Serdes.Long()));

        KTable<Windowed<String>, Long> wordCountSlidingWindows = textLines
                .mapValues(line -> line.toLowerCase())
                .flatMapValues(line -> Arrays.asList(line.split("\\W+")))
                .selectKey((key, word) -> word)
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofSeconds(5)).advanceBy(Duration.ofSeconds(2)))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(swStoreName));

        wordCountSlidingWindows.toStream()
                .map((key, wcounts) -> new KeyValue<>(key.toString().substring(
                        1, key.toString().indexOf("@")) + " " +
                        key.window().startTime() + "->" +
                        key.window().endTime(), wcounts))
                .to("wco-sw", Produced.with(Serdes.String(), Serdes.Long()));

        // create streams fro the builder and start it
        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> streams.close()));
        while (true) {
            try {
                Thread.sleep(10000);
                checkStore(streams, wcStoreName);
                checkWindowsStore(streams, twStoreName);
            } catch (InterruptedException iex) {
                iex.printStackTrace();
            }
        }

    }

    static void checkStore(KafkaStreams streams, String storename) {
        if (streams.state() == KafkaStreams.State.RUNNING) {
            ReadOnlyKeyValueStore<String, Long> kvs =
                    streams.store(storename, QueryableStoreTypes.keyValueStore());
            KeyValueIterator<String, Long> range = kvs.range("all", "streams");
            while (range.hasNext()) {
                KeyValue<String, Long> next = range.next();
                if (next.value > 20)
                    System.out.println("Conditional count for " + next.key + ":" + next.value);
            }
        }
    }

    static void checkWindowsStore(KafkaStreams streams, String storeName) {
        if (streams.state() == KafkaStreams.State.RUNNING) {
            ReadOnlyWindowStore<String, Long> wtwStore =
                    streams.store(storeName, QueryableStoreTypes.windowStore());
            ReadOnlyWindowStore<String, Long> wswStore =
                    streams.store("sw-store", QueryableStoreTypes.windowStore());
            long timeFrom = 0;
            long timeTo = System.currentTimeMillis();
            WindowStoreIterator<Long> iterator = wtwStore.fetch("check", timeFrom, timeTo);
            while (iterator.hasNext()) {
                KeyValue<Long, Long> next = iterator.next();
                long windowTimestamp = next.key;
                System.out.println("Count of 'check' @ time " + windowTimestamp + " is " + next.value);
            }
            KeyValueIterator<Windowed<String>, Long> allTWIterator = wtwStore.fetchAll(timeFrom, timeTo);
            while (allTWIterator.hasNext()) {
                KeyValue<Windowed<String>, Long> next = allTWIterator.next();
                System.out.println(next.key.toString().substring(1, next.key.toString().indexOf("@"))
                        + "," + next.key.window().startTime() + "->" +
                        next.key.window().endTime() + " - " + next.value);
            }

        }
    }
}
