package producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Random;

public class ProducerDemo {
    public static void main(String[] args) {
        // create properties for producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, args[0]);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10000);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5000);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "655360");
        // create producer using properties
        KafkaProducer<String, String> producer = new KafkaProducer<String,
                String>(props);

        // create some producer records
        ProducerRecord<String, String> record = new ProducerRecord<String,
                String>("ide_topic", "hello from the ide");

        // send the records to the broker
        producer.send(record);
        producer.flush();
        try {
            while (true) {
                generateRandomProducerRecord("ide_topic", 10).forEach(prec ->
                        producer.send(prec, new Callback() {
                            @Override
                            public void onCompletion(RecordMetadata metadata, Exception exception) {
                                System.out.println("callback received at " + utils.RoughWork.getCurrentTime());
                            }
                        }));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            producer.close();
        }
    }

    private static ArrayList<ProducerRecord> generateRandomProducerRecord(String topic, int noRecs) {
        String[] cities = new String[]{"Mumbai", "Delhi", "Chennai",
                "Kolkatta", "Jaipur", "Bengaluru", "Hyderabad", "Ludhiana"};
        String[] outcomes = new String[]{"W", "L", "D"};
        ArrayList<ProducerRecord> precs = new ArrayList<>();
        for (int i = 0; i < noRecs; i++) {
            ProducerRecord<String, String> prec =
                    new ProducerRecord<>(topic,
                            cities[new Random().nextInt(cities.length)],
                            outcomes[new Random().nextInt(outcomes.length)]);
            precs.add(prec);
        }
        return precs;
    }

    private static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss:SSS z");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}
