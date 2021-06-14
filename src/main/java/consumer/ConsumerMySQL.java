package consumer;

import com.example.Nseforec;
import com.mysql.cj.MysqlConnection;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;


public class ConsumerMySQL {
    private Connection connectToMySQL(String mysqlServerIP, String mysqlPassword) {
        Connection conn = null;
        try {
            String url = "jdbc:mysql://" + mysqlServerIP + ":3306/testdb?rewriteBatchedStatements=true";
            String user = "kafkauser";
            String password = mysqlPassword;

            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception ex) {
            System.out.println("Problem connecting: " + ex.getMessage());
        }
        return conn;
    }

    private Properties createConsumerConfigs(String bservers, String schemaRegistryURL) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bservers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class.getName());
        props.put("schema.registry.url", schemaRegistryURL);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "nsefogr");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, "true");
        return props;
    }

    private void subscribeAndConsume(String bservers, String schemaRegistryURL, String topic, Connection conn) throws SQLException {
        KafkaConsumer<String, com.example.Nseforec> consumer = new KafkaConsumer<String, Nseforec>(createConsumerConfigs(bservers, schemaRegistryURL));
        consumer.subscribe(Collections.singletonList(topic));
        int batchRecCount = 0;
        int batchNo = 0;
        while (true) {
            ConsumerRecords<String, Nseforec> records = consumer.poll(Duration.ofMillis(100));
            batchRecCount = records.count();
            if (batchRecCount > 0) {
                batchNo++;
                System.out.println("In batch no: " + batchNo + ", received: " + batchRecCount + " records");
                String mysqlStatementToExecute = "Insert into fotbl values ";
                String sql = "Insert ignore into fotbl(trdate, symbol, expirydt, instrument, optiontyp," +
                        "strikepr, closepr, settlepr, contracts, valinlakh, openint, choi) values " +
                        "(?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement statement = conn.prepareStatement(sql);

                for (ConsumerRecord<String, Nseforec> record : records) {
                    Nseforec nseforec = record.value();
                    setStatement(statement, nseforec);
                    statement.addBatch();

                    System.out.println(nseforec.getSymbol() + "," +
                            nseforec.getExpiryDt() + "," +
                            nseforec.getClosepr());
                }
                statement.executeBatch();
            }

        }
    }

    private void setStatement(PreparedStatement stm, Nseforec record) throws SQLException {
        stm.setString(1, record.getTmstamp());
        stm.setString(2, record.getSymbol());
        stm.setString(3, record.getExpiryDt());
        stm.setString(4, record.getInstrument());
        stm.setString(5, record.getOptionTyp());
        stm.setFloat(6, record.getStrikePr());
        stm.setFloat(7, record.getClosepr());
        stm.setFloat(8, record.getSettlepr());
        stm.setInt(9, record.getContracts());
        stm.setFloat(10, record.getValinlakh());
        stm.setInt(11, record.getOpenint());
        stm.setInt(12, record.getChginoi());
//        stm.addBatch();
//        return stm;
    }

    public static void main(String[] args) throws SQLException {
        // args - 0- mysalserverip, 1- mysqlpassword, 2 - bserver, 3 - schemaRegistry, 4 - topic
        try {
            ConsumerMySQL consumerMySQL = new ConsumerMySQL();
            Connection connection = consumerMySQL.connectToMySQL(args[0], args[1]);
//        System.out.println(connection.isValid(1000));
            consumerMySQL.subscribeAndConsume(args[2], args[3], args[4], connection);
        } catch(Exception ex){
            ex.printStackTrace();
        }
    }


}
