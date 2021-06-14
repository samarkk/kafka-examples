package consumer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerHDFSSink {
    static String destPathString = "hdfs://192.168.181.138:8020/user/samar/nsefodata.txt";
    static String bservers = "192.168.181.138:9092";

    public static void main(String[] args) throws IOException, URISyntaxException {
        // args - 0 - bservers, 1 - group, 2 - topic, 3 -  path, 4 - hdfs connection
        ConsumerHDFSSink chds = new ConsumerHDFSSink();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(
                chds.createConsumerConfig(args[0], args[1]));
        String topic = args[2];
        consumer.subscribe(Collections.singletonList(topic));
        Path destPath = new Path(args[3]);
        String hdfsConnString = args[4];
        int batchNo = 0;
        int batchRecords = 0;
        int totBatchRecords = 0;
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> consumer.close()));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            StringBuffer sb = new StringBuffer();
            for (ConsumerRecord<String, String> record : records) {
                batchRecords = records.count();
                if (batchRecords > 0) {
                    sb.append(record.value() + "\n");
                }
            }
            if (batchRecords > 0) {
                totBatchRecords += batchRecords;
                chds.writeLineIntoHDFS(hdfsConnString,destPath, sb.toString());
                System.out.println("Batch no " + batchNo + " written, total records " +
                        "written so far " + totBatchRecords);
                batchNo += 1;

            }
            batchRecords = 0;
        }
    }


    private Properties createConsumerConfig(String bservers, String groupId) {
        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "5000");
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return props;
    }

    private void writeLineIntoHDFS(String hdfsConnectionString, Path destPath, String line) throws IOException, URISyntaxException {
        org.apache.hadoop.conf.Configuration conf = new Configuration();
        conf.set("fs.defaultFS", hdfsConnectionString);
//        conf.set("fs.hdfs.impl",
//                org.apache.hadoop.hdfs.DistributedFileSystem.class.getName()
//        );
//        conf.set("fs.file.impl",
//                org.apache.hadoop.fs.LocalFileSystem.class.getName()
//        );
        conf.setBoolean("dfs.support.append", true);
        FileSystem fs = FileSystem.get(conf);
        FSDataOutputStream out = null;
        PrintWriter writer = null;
        if (!fs.exists(destPath)) {
            out = fs.create(destPath, (short)1);
            out.close();
        }
        out = fs.append(destPath);
        writer = new PrintWriter(out);
        writer.append(line + "\n");
        writer.flush();
        out.flush();
        writer.close();
        out.close();
        fs.close();
    }
}
