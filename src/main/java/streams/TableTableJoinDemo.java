package streams;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

public class TableTableJoinDemo {
    public static void main(String[] args) {
        String bservers = args.length > 0 ? args[0] : "52.234.178.74:9092";
        String playerTopic = "player-team";
        String lastScoreTopic = "player-score";
        String outputTopic = "player-team-score";
        // create streams configuration properties
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "ttjapp");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//        props.put(StreamsConfig.STATE_DIR_CONFIG, "/home/azureuser/kstapp");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        StreamsBuilder builder = new StreamsBuilder();
        KTable<String, String> playerTeamsTable = builder.table(playerTopic);
        KTable<String, Long> playerScoresTable = builder.table(lastScoreTopic,
                Consumed.with(Serdes.String(), Serdes.Long()));

//        final String storeName = "joined-store";

//        playerTeamsTable.join()
        playerTeamsTable.join(playerScoresTable, (team, score) -> team + "/" + score)
                .toStream()
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        Topology streamTopology = builder.build();
        KafkaStreams streams = new KafkaStreams(streamTopology, props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        
    }
}
