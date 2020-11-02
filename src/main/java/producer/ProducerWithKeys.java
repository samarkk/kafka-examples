import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerWithKeys {
    // create configuration
    private Properties createProducerConfig(){
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        return  props;
    }

    // create a producer
    private KafkaProducer createProducer(){
        Properties props = this.createProducerConfig();
        KafkaProducer<String,String > producer = new KafkaProducer<String, String>(props);
        return producer;
    }

    // run method
    private void run() throws ExecutionException, InterruptedException {
        KafkaProducer producer = this.createProducer();
        for(int n = 0; n < 10; n++){
            final ProducerRecord record = new ProducerRecord("ide_topic", "id_" +n,"Record no: "+
                    n + " from the ide");
            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if(e == null) {
                        String msgToPrint = "Topic: " + recordMetadata.topic() +
                                "\nPartition: " + recordMetadata.partition() +
                                "\nOffset: " + recordMetadata.offset() +
                                "\nKey: " + record.key() +
                                "\nvalue:" + record.value();
                        System.out.println(msgToPrint);
                    } else {
                        System.out.println(e.getStackTrace());
                    }
                }
            });
        }
        producer.flush();
        producer.close();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new ProducerWithKeys().run();
    }
}
