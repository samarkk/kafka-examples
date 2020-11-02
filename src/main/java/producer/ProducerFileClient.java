import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ProducerFileClient {
    // create configuration
    private Properties createProducerConfig(){
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        return  props;
    }

    // create a producer
    private KafkaProducer createProducer(){
        Properties props = this.createProducerConfig();
        KafkaProducer<String,String > producer = new KafkaProducer<String, String>(props);
        return producer;
    }

    private void runProducer(String topic, String filePath) throws IOException {
        KafkaProducer<String, String> producer = createProducer();
        FileReader fr = new FileReader(filePath);
        BufferedReader br = new BufferedReader(fr);
        br.readLine();
        Long t1 = System.currentTimeMillis();
        String line;
        while((line = br.readLine()) != null){
            ProducerRecord record = new ProducerRecord(topic, createKey(line), line);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if(e != null){
                        System.out.println("Exception happned");
                        e.printStackTrace();
                    } else {
                        System.out.println("The record was produced successfully to Kafka");
                    }
                }
            });
        }
        producer.close();
        Long t2 = System.currentTimeMillis();
        System.out.println("Time taken to read the file and send to the topic in ms: "+
                (t2 -t1));
    }

    public static void main(String[] args) throws IOException {
        ProducerFileClient pFC = new ProducerFileClient();
        pFC.runProducer(args[0], args[1]);
    }
    private  String createKey(String rval) {
        String[] splits = rval.split(",");
        String key = splits[1] + splits[2] + splits[14] + splits[0] + splits[4] + splits[3];
        return key;
    }

}
