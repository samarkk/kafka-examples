package streams.statelessprocessing.language;

import streams.statelessprocessing.serialization.model.EntitySentiment;
import streams.statelessprocessing.serialization.model.Tweet;
import java.util.List;

public interface LanguageClient {
    public Tweet translate(Tweet tweet, String targetLanguage);
    public List<EntitySentiment> getEntitySentiment(Tweet tweet);
}
