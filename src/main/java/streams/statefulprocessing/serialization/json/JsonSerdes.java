package streams.statefulprocessing.serialization.json;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import streams.statefulprocessing.HighScores;
import streams.statefulprocessing.model.Player;
import streams.statefulprocessing.model.Product;
import streams.statefulprocessing.model.ScoreEvent;
import streams.statefulprocessing.model.join.Enriched;
import streams.statefulprocessing.model.join.ScoreWithPlayer;

public class JsonSerdes {
    public static Serde<HighScores> HighScores() {
        JsonSerializer<HighScores> serializer = new JsonSerializer<>();
        JsonDeserializer<HighScores> deserializer = new JsonDeserializer<>(HighScores.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<Enriched> Enriched() {
        JsonSerializer<Enriched> serializer = new JsonSerializer<>();
        JsonDeserializer<Enriched> deserializer = new JsonDeserializer<>(Enriched.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<Player> Player() {
        JsonSerializer<Player> serializer = new JsonSerializer<>();
        JsonDeserializer<Player> deserializer = new JsonDeserializer<>(Player.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
    public static Serde<Product> Product() {
        JsonSerializer<Product> serializer = new JsonSerializer<>();
        JsonDeserializer<Product> deserializer = new JsonDeserializer<>(Product.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
    public static Serde<ScoreEvent> ScoreEvent() {
        JsonSerializer<ScoreEvent> serializer = new JsonSerializer<>();
        JsonDeserializer<ScoreEvent> deserializer = new JsonDeserializer<>(ScoreEvent.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
    public static Serde<ScoreWithPlayer> ScoreWithPlayer() {
        JsonSerializer<ScoreWithPlayer> serializer = new JsonSerializer<>();
        JsonDeserializer<ScoreWithPlayer> deserializer = new JsonDeserializer<>(ScoreWithPlayer.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}
