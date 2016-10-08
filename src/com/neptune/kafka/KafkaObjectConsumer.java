package com.neptune.kafka;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

/**
 * 在KafkaHighLevelConsumer的基础上进行改进，支持接收自定义的复合类对象，改类必须实现Serializable接口，各方法用法与KafkaHighLevelConsumer相同
 * (存在问题：consumer连接使用后没有调用close()释放资源，暂未出现问题，需要处理)
 *
 * @author Administrator
 */
public class KafkaObjectConsumer {
    private String topic;
    private String brokerList;
    private String groupID;
    private KafkaConsumer<Serializable, Serializable> consumer = null;

    /**
     * @param kafkaBrokers  kafka服务器节点，格式为"hostname:port,hostname:port,..."
     * @param consumerGroup 消费者分组名称，每个分组只有一个消费者能消费该topic
     * @param topic         topic名称
     */
    public KafkaObjectConsumer(String kafkaBrokers, String consumerGroup, String topic) {
        brokerList = kafkaBrokers;
        groupID = consumerGroup;
        this.topic = topic;
        consumer = CreateConsumer();
        consumer.subscribe(Arrays.asList(topic));
    }

    /**
     * 初始化consumer
     *
     * @return
     */
    private KafkaConsumer<Serializable, Serializable> CreateConsumer() {
        Properties pro = new Properties();
        pro.put("bootstrap.servers", brokerList);
        pro.put("group.id", groupID);
        pro.put("enable.auto.commit", "false");
        pro.put("auto.commit.interval.ms", "1000");
        pro.put("session.timeout.ms", "30000");
        pro.put("key.deserializer", "com.neptune.KafkaDeserializer");
        pro.put("value.deserializer", "com.neptune.KafkaDeserializer");
        return new KafkaConsumer<>(pro);
    }

    /**
     * @param minterface
     */
    public void getMessage(MethodInterface minterface) {
        final int minBatchSize = 0;// 可用于将消息压缩后发送，减少IO次数，提升效率
        List<ConsumerRecord<Serializable, Serializable>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<Serializable, Serializable> records = consumer.poll(0);// 问题在这里，将该参数设置为0，可以即使返回目前可读的所有消息，设置为其他值则导致连接速度很慢且读到的消息为空
            for (ConsumerRecord<Serializable, Serializable> record : records) {
                buffer.add(record);
            }
            // 当消息积累到一定数量后对每条消息进行处理
            if (buffer.size() > minBatchSize) {
                for (ConsumerRecord<Serializable, Serializable> element : buffer) {
                    minterface.dealWithData(element.value());// 处理方法应实现MethodInterface接口
                }
                consumer.commitSync();// 提交确认信息，阻塞式的更新offset
                buffer.clear();
            }
            // System.out.println("end of while");
        }
    }

    /**
     * @param minterface
     * @param minBatchSize
     */
    public void getMessage(MethodInterface minterface, int minBatchSize) {
        List<ConsumerRecord<Serializable, Serializable>> buffer = new ArrayList<>();
        // 直到消息数量大于指定的数量为止，不断获取消息
        while (buffer.size() < minBatchSize) {
            ConsumerRecords<Serializable, Serializable> records = consumer.poll(0);// 问题在这里，将该参数设置为0，可以即使返回目前可读的所有消息，设置为其他值则导致连接速度很慢且读到的消息为空
            for (ConsumerRecord<Serializable, Serializable> record : records) {
                buffer.add(record);
            }
        }
        for (ConsumerRecord<Serializable, Serializable> element : buffer) {
            minterface.dealWithData(element.value());// 处理方法应实现MethodInterface接口
        }
        consumer.commitSync();// 提交确认信息，阻塞式的更新offset
    }

    /**
     * @param minBatchSize
     * @return
     */
    public List<Serializable> getMessage(int minBatchSize) {
        List<ConsumerRecord<Serializable, Serializable>> buffer = new ArrayList<>();
        List<Serializable> values = new ArrayList<>();
        // 直到消息数量大于指定的数量为止，不断获取消息
        while (buffer.size() < minBatchSize) {
            ConsumerRecords<Serializable, Serializable> records = consumer.poll(0);// 问题在这里，将该参数设置为0，可以即使返回目前可读的所有消息，设置为其他值则导致连接速度很慢且读到的消息为空
            for (ConsumerRecord<Serializable, Serializable> record : records) {
                buffer.add(record);
            }
            System.out.println(buffer.isEmpty());
        }
        for (ConsumerRecord<Serializable, Serializable> element : buffer) {
            values.add(element.value());
        }
        return values;
    }

    /**
     *
     */
    public void commit() {
        consumer.commitSync();
    }

    /**
     * 上传自己设定的offset，可以使下次读取从该offset处开始，不能过度小于当前offset，否则由于poll超时而报错，或指定offset已被删除
     *
     * @param offset
     */
    private void setOffset(long offset) {
        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<TopicPartition, OffsetAndMetadata>();
        offsets.put(new TopicPartition(topic, 0), new OffsetAndMetadata(offset));
        consumer.commitSync(offsets);
    }

    /**
     * 比较当前offset与最新的offset，若差值大于指定的参数，则从最新的offset处开始读取
     *
     * @param difference 当前offset与最新offset之间的最大差值，若检测出两者之差大于该值，则将从最新offset处开始读取
     */
    public void compareOffsets(long difference) {
        /*
		 * long committedOffset = getCommittedOffset(); long latestOffset =
		 * KafkaOffset.getLatestOffset(topic, groupID, port, brokerIP); if
		 * (latestOffset == -1) return; else { if (latestOffset -
		 * committedOffset >= difference) setOffset(latestOffset); }
		 */
        long committedOffset = getCommittedOffset();
        long latestOffset = -1;
        // 分离以逗号隔开的多可地址
        String[] brokers = brokerList.split(",");
        for (String broker : brokers) {
            // 分离地址中的IP和端口号
            String[] ids = broker.split(":");
            String seed = ids[0];
            int port = Integer.valueOf(ids[1]);
            long offset = KafkaOffset.getLatestOffset(topic, groupID, port, seed);
            if (offset != -1)
                latestOffset = offset;
        }
        if (latestOffset == -1)
            return;
        else if (latestOffset - committedOffset >= difference)
            setOffset(latestOffset);
    }

    /**
     * 获取consumer最近确认上传的offset
     *
     * @return
     */
    private long getCommittedOffset() {
        return consumer.committed(new TopicPartition(topic, 0)).offset();
    }


}



