package streams;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.HostInfo;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;

import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
public class WordCountInteractiveQueriesExample {
    static final String TEXT_LINES_TOPIC = "TextLinesTopic";
    static String DEFAULT_HOST = "localhost";

    public static void main(final String[] args) throws Exception {
        if (args.length == 0 || args.length > 3) {
            throw new IllegalArgumentException("usage: ... <hostForRestEndPoint> <portForRestEndPoint> [<bootstrap.servers> (optional)]");
        }
        DEFAULT_HOST = args[0];
        final int port = Integer.parseInt(args[1]);
        final String bootstrapServers = args.length > 1 ? args[2] : "localhost:9092";

        final Properties streamsConfiguration = new Properties();
        // Give the Streams application a unique name.  The name must be unique in the Kafka cluster
        // against which the application is run.
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "interactive-queries-example");
        streamsConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "interactive-queries-example-client");
        // Where to find Kafka broker(s).
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        // Set the default key serde
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        // Set the default value serde
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        // Provide the details of our embedded http service that we'll use to connect to this streams
        // instance and discover locations of stores.
        streamsConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, DEFAULT_HOST + ":" + port);
        final File example = Files.createTempDirectory(new File("/tmp").toPath(), "example").toFile();
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, example.getPath());

        final KafkaStreams streams = createStreams(streamsConfiguration);
        // Always (and unconditionally) clean local state prior to starting the processing topology.
        // We opt for this unconditional call here because this will make it easier for you to play around with the example
        // when resetting the application for doing a re-run (via the Application Reset Tool,
        // http://docs.confluent.io/current/streams/developer-guide.html#application-reset-tool).
        //
        // The drawback of cleaning up local state prior is that your app must rebuilt its local state from scratch, which
        // will take time and will require reading all the state-relevant data from the Kafka cluster over the network.
        // Thus in a production scenario you typically do not want to clean up always as we do here but rather only when it
        // is truly needed, i.e., only under certain conditions (e.g., the presence of a command line flag for your app).
        // See `ApplicationResetExample.java` for a production-like example.
        streams.cleanUp();
        // Now that we have finished the definition of the processing topology we can actually run
        // it via `start()`.  The Streams application as a whole can be launched just like any
        // normal Java application that has a `main()` method.
        streams.start();

        // Start the Restful proxy for servicing remote access to state stores
        final WordCountInteractiveQueriesRestService restService = startRestProxy(streams, DEFAULT_HOST, port);

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                streams.close();
                restService.stop();
            } catch (final Exception e) {
                // ignored
            }
        }));
    }


    static WordCountInteractiveQueriesRestService startRestProxy(final KafkaStreams streams,
                                                                 final String host,
                                                                 final int port) throws Exception {
        final HostInfo hostInfo = new HostInfo(host, port);
        final WordCountInteractiveQueriesRestService
                wordCountInteractiveQueriesRestService = new WordCountInteractiveQueriesRestService(streams, hostInfo);
        wordCountInteractiveQueriesRestService.start(port);
        return wordCountInteractiveQueriesRestService;
    }

    static KafkaStreams createStreams(final Properties streamsConfiguration) {
        final Serde<String> stringSerde = Serdes.String();
        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String>
                textLines = builder.stream(TEXT_LINES_TOPIC, Consumed.with(Serdes.String(), Serdes.String()));

        final KGroupedStream<String, String> groupedByWord = textLines
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                .groupBy((key, word) -> word, Grouped.with(stringSerde, stringSerde));

        // Create a State Store for with the all time word count
        groupedByWord.count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("word-count")
                .withValueSerde(Serdes.Long()));

        // Create a Windowed State Store that contains the word count for every
        // 1 minute
        groupedByWord.windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as("windowed-word-count")
                        .withValueSerde(Serdes.Long()));

        return new KafkaStreams(builder.build(), streamsConfiguration);
    }
}
