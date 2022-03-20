package streams.statelessprocessing.serialization.json;

import com.google.gson.Gson;
import org.apache.kafka.common.serialization.Serializer;
import streams.statelessprocessing.serialization.model.EntitySentiment;

import java.nio.charset.StandardCharsets;

public class EntitySentimentSerializer implements Serializer<EntitySentiment> {
    private Gson gson = new Gson();

    @Override
    public byte[] serialize(String topic, EntitySentiment entitySentiment) {
        if (entitySentiment == null) return null;
        return gson.toJson(entitySentiment).getBytes(StandardCharsets.UTF_8);
    }
}
