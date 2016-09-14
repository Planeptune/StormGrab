package com.neptune.kafka;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

/**
 * 反序列化类
 */
public class KafkaDeserializer implements Deserializer<Serializable> {
    /**
     *
     */
    public KafkaDeserializer() {

    }

    /**
     *
     */
    public Serializable deserialize(String topic, byte[] data) {
        Object obj = new Object();
        try {
            ByteArrayInputStream ip = new ByteArrayInputStream(data);
            ObjectInputStream objip = new ObjectInputStream(ip);
            obj = objip.readObject();
            objip.close();
            ip.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return (Serializable) obj;
    }

    /**
     *
     */
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    /**
     *
     */
    public void close() {

    }
}