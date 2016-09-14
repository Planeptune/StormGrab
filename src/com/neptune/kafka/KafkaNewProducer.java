package com.neptune.kafka;

import java.io.Serializable;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * 更简单的新producer
 *
 * @author Administrator
 */
public class KafkaNewProducer implements Serializable {
    public org.apache.kafka.clients.producer.KafkaProducer<String, String> producer = null;

    /**
     * 初始化用于发送的KafkaProducer对象
     *
     * @param brokerList kafka服务器节点地址
     */
    public KafkaNewProducer(String brokerList) {
        Properties pro = new Properties();
        pro.put("bootstrap.servers", brokerList);
        pro.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        pro.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(pro);
    }

    /**
     * 向指定的topic发送消息
     *
     * @param topic
     * @param message
     * @throws Exception
     */
    public void send(String topic, String message) throws Exception {
        if (producer != null) {
            producer.send(new ProducerRecord<String, String>(topic, message));
            // System.out.println("Send:" + message);
        } else {
            System.out.println("Producer initializing failed!");
            throw new Exception("Producer is null!");
        }
    }

    /**
     * 关闭producer的连接
     */
    public void close() {
        if (producer != null)
            producer.close();
    }

}