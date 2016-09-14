package com.neptune.kafka;

import java.io.Serializable;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * 新增producer，支持传输自定义的复合类对象，传输的对象需要实现Serializable接口
 *
 * @author Administrator
 */
public class KafkaObjectProducer {
    public org.apache.kafka.clients.producer.KafkaProducer<String, Serializable> producer = null;

    /**
     * 初始化用于发送的KafkaProducer对象，参数为kafka服务器节点，不一定是集群中所有节点的地址，但为了防止由于指定节点故障导致无法发现集群，最好尽量多指定节点地址
     *
     * @param brokerList kafka服务器节点地址
     */
    public KafkaObjectProducer(String brokerList) {
        Properties pro = new Properties();
        pro.put("bootstrap.servers", brokerList);
        pro.put("key.serializer", "com.neptune.KafkaSerializer");
        pro.put("value.serializer", "com.neptune.KafkaSerializer");
        this.producer = new org.apache.kafka.clients.producer.KafkaProducer<>(pro);
    }

    /**
     * 发送复合类的对象
     *
     * @param topic
     * @param message
     * @throws Exception
     */
    public void send(String topic, Serializable message) throws Exception {
        if (producer != null) {
            producer.send(new ProducerRecord<String, Serializable>(topic, message));
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