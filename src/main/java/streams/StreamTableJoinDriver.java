package streams;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class StreamTableJoinDriver {
    public static void main(String[] args) {
//        String bservers = args.length > 0 ? args[0] : "skkvm.eastus.cloudapp.azure.com:9092";
        String bservers = args[0];
        Properties uRegPropcs = new Properties();
        uRegPropcs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        uRegPropcs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        uRegPropcs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());

        // create producer using properties
        KafkaProducer<String, String> producerUserRegion = new KafkaProducer<String,
                String>(uRegPropcs);

        Properties uClickProps = new Properties();
        uClickProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        uClickProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class.getName());
        uClickProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                LongSerializer.class.getName());

        // create producer using properties
        KafkaProducer<String, Long> producerUserClicks = new KafkaProducer<String,
                Long>(uClickProps);

        for (ProducerRecord<String, String> ptrec : createUserRegionRecords()) {
            producerUserRegion.send(ptrec, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    System.out.println("user region record sent successfully");
                }
            });
        }
        producerUserRegion.close();

        for (ProducerRecord<String, Long> psrec : createUserClickRecords()) {
            producerUserClicks.send(psrec, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    System.out.println("user click record sent successfully");
                }
            });
        }
        producerUserClicks.close();
    }

    static ArrayList<ProducerRecord<String, String>> createUserRegionRecords() {
        final List<KeyValue<String, String>> userRegions = Arrays.asList(
                new KeyValue<>("alice", "asia"),   /* Alice lived in Asia originally... */
                new KeyValue<>("bob", "americas"),
                new KeyValue<>("chao", "asia"),
                new KeyValue<>("dave", "europe"),
                new KeyValue<>("alice", "europe"), /* ...but moved to Europe some time later. */
                new KeyValue<>("eve", "americas"),
                new KeyValue<>("fang", "asia"),
                new KeyValue<>("rodrigues", "south americas")
        );
        ArrayList<ProducerRecord<String, String>> userRegionRecords = new ArrayList<>();
        userRegions.forEach(uregkv -> {
            ProducerRecord<String, String>  userRegionRecord = new ProducerRecord<>("user-regions",
                    uregkv.key, uregkv.value);
            userRegionRecords.add(userRegionRecord);
        });
        return userRegionRecords;
    }

    static ArrayList<ProducerRecord<String, Long>> createUserClickRecords() {
        final List<KeyValue<String, Long>> userClicks = Arrays.asList(
                new KeyValue<>("alice", 13L),
                new KeyValue<>("bob", 4L),
                new KeyValue<>("chao", 25L),
                new KeyValue<>("bob", 19L),
                new KeyValue<>("dave", 56L),
                new KeyValue<>("eve", 78L),
                new KeyValue<>("alice", 40L),
                new KeyValue<>("fang", 99L),
                new KeyValue<>("rodrigues", 100L)
        );
        ArrayList<ProducerRecord<String, Long>> userClicksRecords = new ArrayList<>();
        userClicks.forEach(uclickkv -> {
            ProducerRecord<String, Long>  userClickRecord = new ProducerRecord<>("user-clicks",
                    uclickkv.key, uclickkv.value);
            userClicksRecords.add(userClickRecord);
        });
        return userClicksRecords;
    }
}

