package streams.statelessprocessing;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import streams.statelessprocessing.language.DummyClient;
import streams.statelessprocessing.language.GcpClient;
import streams.statelessprocessing.language.LanguageClient;
import streams.statelessprocessing.serialization.model.EntitySentiment;
import streams.statelessprocessing.serialization.model.Tweet;
import streams.statelessprocessing.serialization.json.*;

import java.util.List;

public class CryptoTopology {
    public static List<String> terms;

    public static Topology build() {
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") != null) {
            return build(new GcpClient());
        }
        return build(new DummyClient());
    }

    public static Topology build(LanguageClient languageClient) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<byte[], Tweet> stream = builder.stream("tweets",
                Consumed.with(Serdes.ByteArray(), new TweetSerdes()));
        stream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-stream"));

        // filter out retweets
        KStream<byte[], Tweet> filtered =
                stream.filterNot(
                        (key, tweet) -> {
                            return tweet.isRetweet();
                        });

        // create predicates for filtering english and non-english brances
        Predicate<byte[], Tweet> englishTweets =
                (key, tweet) -> tweet.getLang().equals("en");
        Predicate<byte[], Tweet> nonEnglishTweets =
                (key, tweet) -> !tweet.getLang().equals("en");

        // branch based on tweet language
        KStream<byte[], Tweet>[] branches = filtered.branch(englishTweets, nonEnglishTweets);

        // English tweets
        KStream<byte[], Tweet> englishStream = branches[0];
        englishStream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-english"));

        // non-English tweets
        KStream<byte[], Tweet> nonEnglishStream = branches[1];
        nonEnglishStream.print(Printed.<byte[], Tweet>toSysOut().withLabel("tweets-non-english"));

        // for non-English tweets, translate the tweet text first.
        KStream<byte[], Tweet> translatedStream =
                nonEnglishStream.mapValues(
                        (tweet) -> {
                            return languageClient.translate(tweet, "en");
                        });

        // merge the two streams
        KStream<byte[], Tweet> merged = englishStream.merge(translatedStream);

        // enrich with sentiment and salience scores
        // note: the EntitySentiment class is auto-generated from the schema
        // definition in src/main/resurces/avro/entity_sentiment.avsc
        KStream<byte[], EntitySentiment> enriched =
                merged.flatMapValues(
                        (tweet) -> {
                            // perform entity-level sentiment analysis
                            List<EntitySentiment> results = languageClient.getEntitySentiment(tweet);
//                            System.out.println("Results for debug: " + results);
//                            results.stream().forEach(x -> System.out.println(x.getEntity()));
                            // remove all entity results that don't match a currency
                            System.out.println("results size before removeIf: " + results.size());
                            results.removeIf(entitySentiment -> !terms.contains(entitySentiment.getEntity()));
                            System.out.println("results size after removeIf: " + results.size());
                            return results;
                        });

        enriched.to(
                "crypto-sentiment",
                Produced.with(
                        Serdes.ByteArray(), new EntitySentimentSerdes()));

        return builder.build();
    }
}

