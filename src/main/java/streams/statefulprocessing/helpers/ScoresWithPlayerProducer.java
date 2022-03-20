package streams.statefulprocessing.helpers;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import streams.statefulprocessing.model.Player;
import streams.statefulprocessing.model.ScoreEvent;
import streams.statefulprocessing.model.join.ScoreWithPlayer;
import streams.statefulprocessing.serialization.json.JsonSerdes;
import streams.statefulprocessing.serialization.json.JsonSerializer;

import java.util.Properties;

public class ScoresWithPlayerProducer {
    public static void main(String[] args) {
        JsonSerializer<ScoreWithPlayer> scoreWithPlayerJsonSerializer = new JsonSerializer<>();
        System.out.println(scoreWithPlayerJsonSerializer.getClass().getName());
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "master:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerdes.ScoreWithPlayer().getClass().getName());

        ScoreEvent scoreEvent = new ScoreEvent();
        scoreEvent.setScore(100.0);
        scoreEvent.setPlayerId(1L);
        scoreEvent.setProductId(2L);
        Player player = new Player();
        player.setId(10L);
        player.setName("Some Player");
        ScoreWithPlayer scoreWithPlayer = new ScoreWithPlayer(scoreEvent, player);
//        ProducerRecord<String, JsonSerializer<ScoreWithPlayer>> record = new ProducerRecord<String,
//                JsonSerializer<ScoreWithPlayer>>();
//        ProducerRecord<String, JsonSerdes>
    }
}
