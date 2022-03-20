package streams.statelessprocessing.language;

import com.google.common.base.Splitter;
import streams.statelessprocessing.serialization.model.EntitySentiment;
import streams.statelessprocessing.serialization.model.Tweet;
import io.grpc.netty.shaded.io.netty.util.internal.ThreadLocalRandom;

import java.util.ArrayList;
import java.util.List;

public class DummyClient implements LanguageClient {
    @Override
    public Tweet translate(Tweet tweet, String targetLanguage) {
        tweet.setText("Translated: " + tweet.getText());
        return tweet;
    }

    @Override
    public List<EntitySentiment> getEntitySentiment(Tweet tweet) {
        List<EntitySentiment> results = new ArrayList<>();
//        System.out.println("tweet here is: "+ tweet + " and getText is " + tweet.getText());
        // split the tweet text to get a list of words
        // map the words to lower case
        // replacd # with ''
        // for each of the split words obtained create an Entity Sentiment
        // Entity Sentiment as per model - add tweet id, text, entity - i.e the
        // split word, createdAt and doubles for sentiment score,
        // sentiment magnitude and salience
        Iterable<String> words =
                Splitter.on(' ').split(tweet.getText().toLowerCase().replace(
                        "#", ""));
        for (String entity : words) {
            EntitySentiment entitySentiment = new EntitySentiment();

            entitySentiment.setCreatedAt(tweet.getCreatedAt());
            entitySentiment.setId(tweet.getId());
            entitySentiment.setEntity(entity);
            entitySentiment.setText(tweet.getText());
            entitySentiment.setSalience(randomDouble());
            entitySentiment.setSentimentScore(randomDouble());
            entitySentiment.setSentimentMagnitude(randomDouble());

            results.add(entitySentiment);
        }
        return results;
    }

    Double randomDouble() {
        return ThreadLocalRandom.current().nextDouble(0, 1);
    }
}
