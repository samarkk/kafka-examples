package producer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.Properties;

public class SASLSSLProducer {
/*
-Djava.security.auth.login.config=D:/vagpg/kafkasecmon/win_cli_jaas.conf  -Djava.security.krb5.conf=/c/ProgramData/MIT/Kerberos5/krb5.ini  -Djavax.security.auth.useSubjectCredsOnly=true
 */
    /*
-Djava.security.auth.login.config=D:/vagpg/kafkasecmon/kafka_client_jaas.conf  -Djava.security.krb5.conf=/c/ProgramData/MIT/Kerberos5/krb5.ini  -Djavax.security.auth.useSubjectCredsOnly=false
 */
    /*
    java -Djava.security.auth.login.config=/home/vagrant/kafka_client_jaas.conf -cp /vagrant/uber.jar producer.SASLSSLProducer master.e4rlearning.com:9094 /home/vagrant/
     */

    /*
    kafka-console-consumer --bootstrap-server master.e4rlearning.com:9094 --topic ssl-ide-topic  --from-beginning --consumer.config=/home/vagrant/kafka_client_kerberos.properties
     */
    /*
    arguments
    0 - bootstrap server - full hostname - not master etc, 1- the directory
    where configuration files are - on the machines - /vagrant, on client -
    provide the client path,  3-  the jaas conf file, kafka_client_jaas_ide
    .conf for
     the ide and kafka_client_jaas_for_javacp - for checking using the jar on
      the cluster machines, 4 - sun security krb5 debug - set to true when
      problem
      java -cp uber-kafkaproject-1.0-SNAPSHOT.jar producer.SASLSSLProducer master.e4rlearning.com:9094 /vagrant/  kafka_client_jaas_for_javacp.conf false
     */
    public static void main(String[] args) throws IOException {
        String bserver = args[0];
        String secDirLocation = args[1];

//        String principalName = args[2];
//        String keyTabPath = args[3];
//        UserGroupInformation.loginUserFromKeytab(principalName, keyTabPath);
//        System.out.println("Is login keytab based: " + UserGroupInformation.isLoginKeytabBased());
        System.setProperty("java.security.krb5.conf", secDirLocation + "krb5" +
                ".conf");
        System.setProperty("java.security.auth.login.config",
                secDirLocation + args[2]);
        System.setProperty("javax.securi" +
                "ty.auth.useSubjectCredsOnly", "false");
        System.setProperty("sun.security.krb5.debug", args[3]);


        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bserver);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, secDirLocation + "kafka.client.truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "clisec");

//        props.put(SaslConfigs.SASL_JAAS_CONFIG, secDirLocation + "kafka_client_jaas_conf");

//        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, secDirLocation + "kafka.client.keystore.jks");
//        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "clisec");
//        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "clisec");


        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        ProducerRecord<String, String> record = new ProducerRecord<>("ssl-ide-topic", "message from a java program to a secure topic");
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.out.println(exception.getMessage());
            } else {
                System.out.println("sec record sent successfully to secure topic");
            }
        });

        producer.flush();
        producer.close();
    }
}
