package streams.statelessprocessing.serialization.json;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.kafka.common.serialization.Deserializer;
import streams.statelessprocessing.serialization.model.EntitySentiment;

import java.nio.charset.StandardCharsets;

public class EntitySentimentDeserializer implements Deserializer<EntitySentiment> {
    private Gson gson =
            new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    @Override
    public EntitySentiment deserialize(String topic, byte[] bytes) {
        if (bytes == null)
            return null;
        return gson.fromJson(new String(bytes, StandardCharsets.UTF_8),
                EntitySentiment.class);
    }
}
