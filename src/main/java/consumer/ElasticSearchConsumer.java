package consumer;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ElasticSearchConsumer {
    private final String hostName;
    private final String bservers;
    private final String groupId;
    private final String topic;

    public ElasticSearchConsumer(String hostName, String bservers, String groupId, String topic) {
        this.hostName = hostName;
        this.bservers = bservers;
        this.groupId = groupId;
        this.topic = topic;
    }

    private RestHighLevelClient createClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(this.hostName,
                9200, "http"));
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }

    private KafkaConsumer<String, String> createConsumer() {
        // create consumer configs
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // disable auto commit of offsets
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100"); // disable auto commit of offsets

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Collections.singletonList(topic));

        return consumer;
    }

    private final JsonParser jsonParser = new JsonParser();

    private String extractIdFromTweet(String tweetJson) {
        String id = jsonParser.parse(tweetJson)
                .getAsJsonObject()
                .get("id")
                .getAsString();
//        System.out.println("tweetJson is " + tweetJson + " and id is " + id);
        return id;
    }

    public static void main(String[] args) throws IOException {
        ElasticSearchConsumer elasticSearchConsumer = new ElasticSearchConsumer(args[0], args[1], args[2], args[3]);
        Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
        RestHighLevelClient client = elasticSearchConsumer.createClient();
        KafkaConsumer<String, String> consumer = elasticSearchConsumer.createConsumer();

        while (true) {
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100));
            int recCount = records.count();
            BulkRequest bulkRequest = new BulkRequest();
            if (recCount > 0) {
                logger.info("Received " + recCount + " records");
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        String id = elasticSearchConsumer.extractIdFromTweet(record.value());
                        IndexRequest indexRequest = new IndexRequest("tweets")
                                .source(record.value(), XContentType.JSON)
                                .id(id);
                        bulkRequest.add(indexRequest);
                    } catch (NullPointerException e) {
                        logger.warn("skipping bad data: " + record.value());
                    }
                }

                BulkResponse bulkItemResponses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                logger.info("Committing offsets...");
                consumer.commitSync();
                logger.info("Offsets have been committed");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
