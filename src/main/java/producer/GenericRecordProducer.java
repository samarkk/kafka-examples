package producer;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class GenericRecordProducer {
    public static void main(String[] args) {
        Schema.Parser parser = new Schema.Parser();
        String genericSchemaString = "{\n" +
                "    \"type\": \"record\",\n" +
                "    \"namespace\": \"com.example\",\n" +
                "    \"name\": \"GCustomer\",\n" +
                "    \"fields\": [\n" +
                "        {\"name\": \"first_name\", \"type\": \"string\",\"doc\": \"Customer first name\" },\n" +
                "        {\"name\": \"last_name\", \"type\": \"string\",\"doc\": \"Customer last name\"},\n" +
                "        {\"name\":\"age\", \"type\": \"int\",\"doc\":\"Age\"},\n" +
                "        {\"name\":\"height\", \"type\": \"float\"},\n" +
                "        {\"name\":\"likes\", \"type\": {\"type\": \"array\", \"items\": \"string\", \"default\": []}}\n" +
                "    ]\n" +
                "}";
        Schema schema = parser.parse(genericSchemaString);
        GenericRecord genericCustomerRecord = new GenericData.Record(schema);
        genericCustomerRecord.put("first_name", "john");
        genericCustomerRecord.put("last_name", "gulliver");
        genericCustomerRecord.put("age", 19);
        genericCustomerRecord.put("height", 183.4f);
        List<String> likes = new ArrayList<>();
        likes.add("travelling");
        likes.add("preaching");
        genericCustomerRecord.put("likes", likes);

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, args[0]);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", args[1]);

        KafkaProducer<String, GenericRecord> producer = new KafkaProducer<String, GenericRecord>(props);

        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(args[2], genericCustomerRecord);
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                System.out.println("Sent record to topic: " + metadata.topic() + ", partition: " + metadata.partition() + ", offset: " + metadata.offset());
            }
        });
        producer.close();
    }
}
