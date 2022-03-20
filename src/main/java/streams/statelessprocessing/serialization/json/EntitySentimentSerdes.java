package streams.statelessprocessing.serialization.json;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import streams.statelessprocessing.serialization.model.EntitySentiment;

public class EntitySentimentSerdes implements Serde<EntitySentiment> {
    @Override
    public Serializer<EntitySentiment> serializer() {
        return new EntitySentimentSerializer();
    }

    @Override
    public Deserializer<EntitySentiment> deserializer() {
        return new EntitySentimentDeserializer();
    }
}
