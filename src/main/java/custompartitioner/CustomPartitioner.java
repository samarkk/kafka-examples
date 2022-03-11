package custompartitioner;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.InvalidRecordException;

import java.util.Locale;
import java.util.Map;

public class CustomPartitioner implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        int numPartitions = cluster.partitionCountForTopic(topic);
        int partToReturn = 0;

        if (keyBytes == null)
            throw new InvalidRecordException("All messages have a key beginning with" +
                    " a character - lower case or upper");

        if (Character.compare(key.toString().toUpperCase().toCharArray()[0], 'H') > 0 && Character.compare(key.toString().toUpperCase().toCharArray()[0], 'S') < 0) {
            partToReturn = 1;
        }
        if (Character.compare(key.toString().toUpperCase().toCharArray()[0], 'S') >= 0) {
            partToReturn = 2;
        }

        return partToReturn;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}
