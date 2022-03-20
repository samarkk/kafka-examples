package streams.statefulprocessing;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import streams.statefulprocessing.model.Player;
import streams.statefulprocessing.model.Product;
import streams.statefulprocessing.model.ScoreEvent;
import streams.statefulprocessing.model.join.Enriched;
import streams.statefulprocessing.model.join.ScoreWithPlayer;
import streams.statefulprocessing.serialization.json.JsonSerdes;

class LeaderboardTopology {

    public static Topology build() {
        // the builder is used to construct the topology
        StreamsBuilder builder = new StreamsBuilder();

        // register the score events stream
        KStream<String, ScoreEvent> scoreEvents =
                builder
                        .stream("score-events", Consumed.with(Serdes.String(), JsonSerdes.ScoreEvent()))
                        // now marked for re-partitioning
                        .selectKey((k, v) -> v.getPlayerId().toString());

        // create the sharded players table
        KTable<String, Player> players =
                builder.table("players", Consumed.with(Serdes.String(), JsonSerdes.Player()));

        // create the global product table
        GlobalKTable<String, Product> products =
                builder.globalTable("products", Consumed.with(Serdes.String(), JsonSerdes.Product()));

        KStream<String, ScoreWithPlayer> withPlayers = scoreEvents.join(players,
                ScoreWithPlayer::new,
                Joined.with(Serdes.String(), JsonSerdes.ScoreEvent(), JsonSerdes.Player()));
//        withPlayers.print(Printed.toSysOut());

//        KStream<String, String> scoresWithPlayers = withPlayers.mapValues(swp -> "ProductId:" +
//                " " +
//                swp.getScoreEvent().getProductId().toString() +
//                ", PlayerId: " + swp.getScoreEvent().getPlayerId().toString() + ", Name: " + swp.getPlayer().getName() +
//                ", Score: " + swp.getScoreEvent().getScore().toString());
//        scoresWithPlayers.print(Printed.toSysOut());
//        scoresWithPlayers.to("scores-with-players",
//                Produced.with(Serdes.String(), Serdes.String()));

//        KStream<String, ScoreWithPlayer> swpKeyedWithProductId =
//                withPlayers.map((k, swp) ->
//                        new KeyValue<>(swp.getScoreEvent().getProductId().toString(), swp));

        KStream<String, Enriched> withProducts = withPlayers.join(products,
                (key, swp) -> swp.getScoreEvent().getProductId().toString(),
                Enriched::new);
//        withProducts.print(Printed.toSysOut());

        /** Group the enriched product stream */
        KGroupedStream<String, Enriched> grouped =
                withProducts.groupBy(
                        (key, value) -> value.getProductId().toString(),
                        Grouped.with(Serdes.String(), JsonSerdes.Enriched()));

        /** The initial value of our aggregation will be a new HighScores instances */
        Initializer<HighScores> highScoresInitializer = HighScores::new;

        /** The logic for aggregating high scores is implemented in the HighScores.add method */
        Aggregator<String, Enriched, HighScores> highScoresAdder =
                (key, value, aggregate) -> aggregate.add(value);

        /** Perform the aggregation, and materialize the underlying state store for querying */
        KTable<String, HighScores> highScores =
                grouped.aggregate(
                        highScoresInitializer,
                        highScoresAdder,
                        Materialized.<String, HighScores, KeyValueStore<Bytes, byte[]>>
                                // give the state store an explicit name to make it available for interactive
                                // queries
                                        as("leader-boards")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(JsonSerdes.HighScores()));

        highScores.toStream().to("high-scores", Produced.with(
                Serdes.String(), JsonSerdes.HighScores()
        ));

        return builder.build();
    }
}