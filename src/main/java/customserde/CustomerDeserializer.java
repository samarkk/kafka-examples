package customserde;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

public class CustomerDeserializer implements Deserializer<Customer> {
    public void configure(Map configs, boolean isKey) {

    }

    public Customer deserialize(String topic, byte[] bytes) {
        Customer customer = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            customer = mapper.readValue(bytes, Customer.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return customer;
    }

    public void close() {

    }
}

