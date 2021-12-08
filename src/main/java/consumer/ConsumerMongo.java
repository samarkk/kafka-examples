package consumer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ConsumerMongo {
    public static void insertRecordsIntoMongo(ConsumerRecords<String,
            String> records, String mongodbUri) {
        try {
            MongoClient mongoClient = MongoClients.create(mongodbUri);
            MongoDatabase fdb = mongoClient.getDatabase("fdb");
            MongoCollection<Document> foCollection = fdb.getCollection("focollide");

            List<Document> foDocuments = new ArrayList<>();

            for (ConsumerRecord<String, String> record : records) {
                String[] recFields = record.value().split(",");
                String keyForDoc = recFields[1]+recFields[2]+recFields[0]+
                        recFields[14]+ recFields[4]+recFields[3];
                Document foDoc = new Document("_id", keyForDoc).append(
                                "instrument", recFields[0])
                        .append("symbol", recFields[1])
                        .append("expiry_dt", recFields[2])
                        .append("strike_pr", recFields[3])
                        .append("option_typ", recFields[4])
                        .append("openpr", recFields[5])
                        .append("highpr", recFields[6])
                        .append("lowpr", recFields[7])
                        .append("closepr", recFields[8])
                        .append("settlepr", recFields[9])
                        .append("contracts", recFields[10])
                        .append("vlakh", recFields[11])
                        .append("oint", recFields[12])
                        .append("chg_oint", recFields[13])
                        .append("tmstamp", recFields[14]);
                foDocuments.add(foDoc);
            }
            System.out.println("foDocuments has now: " + foDocuments.size() + " documents");
            InsertManyOptions insertManyOptions = new InsertManyOptions();
            insertManyOptions.ordered(false);
            foCollection.insertMany(foDocuments,insertManyOptions);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Please supply the arguments: " +
                    "<bootstrap-server> <groupid> <topic> <mongodburi>");
            System.exit(1);
        }
        // create configs
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, args[0]);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, args[1]);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String,
                String>(props);

        // subscribe to topic
        consumer.subscribe(Collections.singleton(args[2]));

        //run poll loop infinite
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            // send documents fo mongodb
            if (records.count() > 0)
                insertRecordsIntoMongo(records, args[3]);
        }
    }
}
