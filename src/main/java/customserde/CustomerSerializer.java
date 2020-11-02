package customserde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public class CustomerSerializer implements Serializer<Customer> {
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    //    public byte[] serialize(String topic, Customer customer) {
//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        byte[] bytes = null;
//        try {
//            ObjectOutputStream oos = new ObjectOutputStream(bos);
//            oos.writeObject(customer);
//            oos.flush();
//            bytes = bos.toByteArray();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bytes;
//    }
    public byte[] serialize(String topic, Customer data) {
        byte[] retVal = null;
        ObjectMapper mapper = new ObjectMapper();
        try {

            retVal = mapper.writeValueAsString(data).getBytes();
//            retVal = mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    public void close() {

    }
}

