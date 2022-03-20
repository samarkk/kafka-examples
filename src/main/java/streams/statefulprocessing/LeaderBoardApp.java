package streams.statefulprocessing;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.HostInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class LeaderBoardApp {
    public static void main(String[] args) throws Exception {
        Topology topology = LeaderboardTopology.build();

        // we allow the following system properties to be overridden,
        // which allows us to run multiple instances of our app.
        // see the `runFirst` and `runSecond` gradle tasks in build.gradle
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String stateDir = "/home/vagrant/lbstore";
        String endpoint = host + ":" + port;

        // set the required properties for running Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, args[2]);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, args[3]);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        props.put(StreamsConfig.APPLICATION_SERVER_CONFIG, host+":"+port);
        final File example = Files.createTempDirectory(new File("/tmp").toPath(), "example").toFile();
        props.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);

        // build the topology
        System.out.println("Starting Videogame Leaderboard");
        KafkaStreams streams = new KafkaStreams(topology, props);
        // start streaming!
        streams.start();

        // start the REST service
        HostInfo hostInfo = new HostInfo(host, port);
        LeaderboardService service = new LeaderboardService(hostInfo,streams);
        service.start();

//        LeaderboardRestService leaderboardRestService = new LeaderboardRestService(streams, new HostInfo(host,port));
//        leaderboardRestService.start(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                streams.close();
//                leaderboardRestService.stop();
            } catch (final Exception e) {
                // ignored
            }
        }));

    }
}