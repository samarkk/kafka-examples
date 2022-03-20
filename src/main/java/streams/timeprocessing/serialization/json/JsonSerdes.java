package streams.timeprocessing.serialization.json;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import streams.timeprocessing.model.BodyTemp;
import streams.timeprocessing.model.CombinedVitals;
import streams.timeprocessing.model.Pulse;

public class JsonSerdes {

    public static Serde<Pulse> Pulse() {
        JsonSerializer<Pulse> serializer = new JsonSerializer<>();
        JsonDeserializer<Pulse> deserializer = new JsonDeserializer<>(Pulse.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<BodyTemp> BodyTemp() {
        JsonSerializer<BodyTemp> serializer = new JsonSerializer<>();
        JsonDeserializer<BodyTemp> deserializer = new JsonDeserializer<>(BodyTemp.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }

    public static Serde<CombinedVitals> CombinedVitals() {
        JsonSerializer<CombinedVitals> serializer = new JsonSerializer<>();
        JsonDeserializer<CombinedVitals> deserializer = new JsonDeserializer<>(CombinedVitals.class);
        return Serdes.serdeFrom(serializer, deserializer);
    }
}