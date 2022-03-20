package streams.statelessprocessing.serialization.json;

import com.google.gson.Gson;
import streams.statelessprocessing.serialization.model.Tweet;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Serializer;

public class TweetSerializer implements Serializer<Tweet> {
    private Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, Tweet tweet) {
        if (tweet == null) return null;
        return gson.toJson(tweet).getBytes(StandardCharsets.UTF_8);
    }
}
