package com.neptune.kafka;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

/**
 * 序列化与反序列化类，已隐式调用，无需手动使用
 *
 * @author Administrator
 */
public class KafkaSerializer implements Serializer<Serializable> {
    /**
     *
     */
    public KafkaSerializer() {

    }

    /**
     *
     */
    public byte[] serialize(String topic, Serializable data) {
        byte[] B = new byte[2048];
        try {
            // System.out.println("序列化:"+tc.integer);
            ByteArrayOutputStream op = new ByteArrayOutputStream();
            ObjectOutputStream objop = new ObjectOutputStream(op);
            objop.writeObject(data);
            B = op.toByteArray();
            objop.close();
            op.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return B;
    }

    /**
     *
     */
    public void configure(Map<String, ?> configs, boolean iskey) {

    }

    /**
     *
     */
    public void close() {

    }
}