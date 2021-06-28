package producer;

import com.example.Nseforec;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class AvroProducerFileClient {

    private Properties createProducerConfig(String bootstrapServers, String schemaRegistryURL) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE - 1);
        props.put("schema.registry.url", schemaRegistryURL);
        return props;
    }

    private void runProducer(String bootstrapServers, String schemaRegistryURL, String topic, String filePath) throws IOException {
        KafkaProducer<String, Nseforec> producer = new KafkaProducer<String, Nseforec>(createProducerConfig(bootstrapServers, schemaRegistryURL));

        FileReader fr = new FileReader(filePath);

        BufferedReader br = new BufferedReader(fr);
        br.readLine();
        String line;
//        String valToPrint;
        Long t1 = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
            String key = createKey(line);
            Nseforec nseforec = createNSEForec(line);
            ProducerRecord record = new ProducerRecord(topic, key, nseforec);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (e == null) {
                        String valToPrint = "Topic: " + recordMetadata.topic() +
                                "\nPartition: " + recordMetadata.partition() +
                                "\nOffset " + recordMetadata.offset();
                        System.out.println(valToPrint);
                    }
                }
            });
        }
        producer.flush();
        producer.close();
        Long t2 = System.currentTimeMillis();
        System.out.println("Time taken to read the file and send to topic " + (t2 - t1) +
                ", in seconds: " + (t2 - t1) / 1000);
    }

    public static void main(String[] args) throws IOException {
        // args - boootstrap server -0, schema registry url -1, topic - 2, filepath - 3
        new AvroProducerFileClient().runProducer(args[0], args[1], args[2], args[3]);
    }

    private String createKey(String rval) {
        String[] splits = rval.split(",");
        String key = splits[1] + splits[2] + splits[14] + splits[0] + splits[4] + splits[3];
        return key;
    }

    private Nseforec createNSEForec(String line) {
//  INSTRUMENT,SYMBOL,EXPIRY_DT,STRIKE_PR,OPTION_TYP,OPEN,HIGH,LOW,CLOSE,SETTLE_PR,CONTRACTS,VAL_INLAKH,OPEN_INT,CHG_IN_OI,TIMESTAMP,
//  FUTIDX,BANKNIFTY,30-Jan-2020,0,XX,32417.3,32495.75,32225,32285.2,32285.2,95999,620595.55,1299680,-5300,01-JAN-2020,

        String[] colVals = line.split(",");
        Nseforec nserec = Nseforec.newBuilder()
                .setInstrument(colVals[0])
                .setSymbol(colVals[1])
                .setExpiryDt(colVals[2])
                .setStrikePr(Float.parseFloat(colVals[3]))
                .setOptionTyp(colVals[4])
                .setOpenpr(Float.parseFloat(colVals[5]))
                .setHighpr(Float.parseFloat(colVals[6]))
                .setLowpr(Float.parseFloat(colVals[7]))
                .setClosepr(Float.parseFloat(colVals[8]))
                .setSettlepr(Float.parseFloat(colVals[9]))
                .setContracts(Integer.parseInt(colVals[10]))
                .setValinlakh(Float.parseFloat(colVals[11]))
                .setOpenint(Integer.parseInt(colVals[12]))
                .setChginoi(Integer.parseInt(colVals[13]))
                .setTmstamp(colVals[14])
                .build();
        return nserec;
    }
}
