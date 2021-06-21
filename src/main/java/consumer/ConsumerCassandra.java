package consumer;

import com.datastax.oss.driver.api.core.CqlSession;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

public class ConsumerCassandra {
    // provide these are args 0, 1, 2,3
//    static String bservers = "skkvm.eastus.cloudapp.azure.com:9092";
//    static String cassip = "skkvm.eastus.cloudapp.azure.com";
//    static String topic = "nsefotopic";
//    static String grpid = "fingr";

    public static void main(String[] args) {
        new ConsumerCassandra().run(args[0], args[1], args[2], args[3]);
    }

    private Properties createConumserConfig(String bservers, String grpid) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 10000);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, grpid);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "20");
        return props;
    }

    private void run(String bservers, String cassip, String topic, String grpid) {
        CassandraConnector connector = new CassandraConnector();
        // select broadcast_address, cluster_name, data_Center, host_id, rpc_address, listen_address from system.local;
        connector.connect(cassip, 9042, "datacenter1", "finks");
        CqlSession session = connector.getSession();
        Properties consumerProps = createConumserConfig(bservers, grpid);
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(consumerProps);
        consumer.subscribe(Collections.singletonList(topic));
        int rollingTotal = 0;
        int batchNo = 0;
        String query = "";
        int counter = 0;
        int batchSize = 0;
        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

//                for (ConsumerRecord<String, String> record : records) {
//                    pushDataIntoCassandra(session, record.value());
//                }
                batchSize = records.count();
                if (batchSize > 0) {
                    System.out.println("Total records gotten in batch: " + batchNo + ":- " + records.count());
                    batchNo++;
                    query = " BEGIN BATCH ";
                    for (ConsumerRecord<String, String> record : records) {
                        query += createBatchStatementUnit(record.value()) + ' ';
                        counter++;
                        if (counter == batchSize) {
                            query += " APPLY BATCH;";
                        }
                    }
//                    System.out.println("query to execute " + query);
                    session.execute(query);
                }
                counter = 0;

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            connector.close();
        }
    }

    private LocalDate convertStringToDate(String str) {
        String mname = str.substring(3, 6);
        HashMap<String, String> hmap = new HashMap<>();
        hmap.put("JAN", "01");
        hmap.put("FEB", "02");
        hmap.put("MAR", "03");
        hmap.put("APR", "04");
        hmap.put("MAY", "05");
        hmap.put("JUN", "06");
        hmap.put("JUL", "07");
        hmap.put("AUG", "08");
        hmap.put("SEP", "09");
        hmap.put("OCT", "10");
        hmap.put("NOV", "11");
        hmap.put("DEC", "12");

        String strMnameReplaced = str.substring(0, 2) + "/" + hmap.get(mname) + "/" +
                str.substring(7, str.length());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
        LocalDate dt = LocalDate.parse(strMnameReplaced, formatter);
        return dt;
    }

    private void pushDataIntoCassandra(CqlSession session, String recVal) {
        try {
            String[] colVals = recVal.split(",");
            String stToExecute = "Insert into finks.fotable(symbol, expiry, trdate, instrument," +
                    "option_typ, strike_pr, chgoi, contracts, cpr, oi, trdval, tstamp)\n values ('" +
                    colVals[1] + "','" + convertStringToDate(colVals[2].toUpperCase()) +
                    "','" + convertStringToDate(colVals[14]) +
                    "','" + colVals[0] + "','" + colVals[4] + "'," + new BigDecimal(colVals[3]) +
                    "," + colVals[13] + "," + colVals[10] + "," + new BigDecimal(colVals[8]) +
                    "," + colVals[12] + "," + new BigDecimal(colVals[11]) +
                    ",'" + new Timestamp(System.currentTimeMillis()) + "')";
//            System.out.println(stToExecute);
            session.execute(stToExecute);
        } catch (Exception ex) {
            System.out.println("Exception happened in pushDataIntoCassandra in ConsumerCassandra");
            ex.printStackTrace();
        }
    }

    private String createBatchStatementUnit(String recVal) {
        String[] colVals = recVal.split(",");
        String stToExecute = "  Insert into finks.fotable(symbol, expiry, trdate, instrument," +
                "option_typ, strike_pr, chgoi, contracts, cpr, oi, trdval, tstamp)\n values ('" +
                colVals[1] + "','" + convertStringToDate(colVals[2].toUpperCase()) +
                "','" + convertStringToDate(colVals[14]) +
                "','" + colVals[0] + "','" + colVals[4] + "'," + new BigDecimal(colVals[3]) +
                "," + colVals[13] + "," + colVals[10] + "," + new BigDecimal(colVals[8]) +
                "," + colVals[12] + "," + new BigDecimal(colVals[11]) +
                ",'" + new Timestamp(System.currentTimeMillis()) + "');  ";
        return stToExecute;
    }

}
