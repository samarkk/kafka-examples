package streams.statelessprocessing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import streams.statelessprocessing.serialization.model.Tweet;
import streams.statelessprocessing.serialization.json.TweetSerializer;

public class TweetsProducer {

    public static void main(String[] args) throws IOException {
        // args - 0 -bootstrap server, 1 - topic, 2 - tweets file

        // to create the tweet file

        // look at file twint_to_kafka.py in kafka_scripts for how
        // to produce data with twint
        // twint -s 'terms to search' --json  -o filePath to save file
        // so one could go to tmp under D and specify x.json to have file stored at
        // D:/tmp/x.json

        // create properties for producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, args[0]);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                TweetSerializer.class.getName());
//        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "15000");
//        props.put(ProducerConfig.)
//        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
//        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "130000");
        // create producer using properties
        KafkaProducer<String, Tweet> producer = new KafkaProducer<String,
                Tweet>(props);

        FileReader fr = new FileReader(args[2]);
        BufferedReader br = new BufferedReader(fr);
        br.readLine();
        String line = "";
        String topic = args[1];
        Gson gson = new Gson();
        try {
            while ((line = br.readLine())!= null) {
//                    System.out.println("line is: " + line);
                JsonObject tweetJson = gson.fromJson(line, JsonObject.class);
                System.out.println("tweetJson is " + tweetJson.toString());
                String dateTime = tweetJson.get("date").getAsString() +
                        " " + tweetJson.get("time").getAsString();
//                    System.out.println(dateTime);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                long tweetTime =
                        sdf.parse(dateTime).getTime() / 1000L;
                String tweetId = tweetJson.get("id").getAsString();
                Tweet aTweet = new Tweet();
                aTweet.setId(tweetJson.get("id").getAsLong());
                aTweet.setCreatedAt(tweetTime);
                aTweet.setLang(tweetJson.get("language").getAsString());
                aTweet.setRetweet(tweetJson.get("retweet").getAsBoolean());
                aTweet.setText(tweetJson.get("tweet").getAsString().toLowerCase());
                System.out.println("tweet is " + aTweet);
                ProducerRecord<String, Tweet> record =
                        new ProducerRecord<String, Tweet>(topic, tweetId
                                , aTweet);
                producer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata metadata, Exception exception) {
                        String sendDetails =
                                "Producer Record sent to topic: " + metadata.topic() +
                                        "\nPartition: " + metadata.partition() +
                                        "\nKey: " + record.key() +
                                        "\nOffset: " + metadata.offset() +
                                        "\nTimestamp: " + metadata.timestamp();
                        System.out.println(sendDetails);
                    }
                });
                producer.flush();
            }
        } catch (Exception ex) {
            System.out.println("Exception underway");
            ex.printStackTrace();
        } finally {
            System.out.println("Finally block in action");
            producer.close();
            br.close();
        }
    }
}

