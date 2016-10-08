package com.neptune.kafka;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.ErrorMapping;
import kafka.common.TopicAndPartition;
import kafka.javaapi.OffsetRequest;
import kafka.javaapi.OffsetResponse;
import kafka.javaapi.PartitionMetadata;
import kafka.javaapi.TopicMetadata;
import kafka.javaapi.TopicMetadataRequest;
import kafka.javaapi.TopicMetadataResponse;
import kafka.javaapi.consumer.SimpleConsumer;

/**
 * 使用高级consumer，可以提供offset的精确控制，当consumer确定完成对消息的处理后手动提交offset，确保故障时可以重读必要的部分
 * 提供了3种获取消息的方法
 * 同时支持比较当前读取的消息是否过多落后于最新的offset，如果过于落后，则直接跳过部分消息，从最新的消息开始，要使用该功能，请手动调用compareOffset(long)方法
 * 单不建议每次读取消息都调用该方法，因为该方法执行一次的时间可能近似于读取一条消息的时间
 * (存在问题：consumer连接使用后没有调用close()释放资源，暂未出现问题，需要处理)
 *
 * @author Administrator
 */
public class KafkaHighLevelConsumer {
    private String topic;
    private String brokerList;
    private String groupID;
    private KafkaConsumer<String, String> consumer = null;

    /**
     * @param kafkaBrokers  kafka服务器节点，格式为"hostname:port,hostname:port,..."
     * @param consumerGroup 消费者分组名称，每个分组只有一个消费者能消费该topic
     * @param topic         topic名称
     */
    public KafkaHighLevelConsumer(String kafkaBrokers, String consumerGroup, String topic) {
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
    private KafkaConsumer<String, String> CreateConsumer() {
        Properties pro = new Properties();
        pro.put("bootstrap.servers", brokerList);
        pro.put("group.id", groupID);
        pro.put("enable.auto.commit", "false");
        pro.put("auto.commit.interval.ms", "1000");
        pro.put("session.timeout.ms", "30000");
        pro.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        pro.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return new KafkaConsumer<>(pro);
    }

    /**
     * 通过死循环不断获取消息，参数继承了接口MethodInterface，用于处理数据
     *
     * @param minterface 实现自己对数据的处理方法的类
     */
    public void getMessage(MethodInterface minterface) {
        final int minBatchSize = 0;// 可用于将消息压缩后发送，减少IO次数，提升效率
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(0);// 问题在这里，将该参数设置为0，可以即使返回目前可读的所有消息，设置为其他值则导致连接速度很慢且读到的消息为空
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }
            // 当消息积累到一定数量后对每条消息进行处理
            if (buffer.size() > minBatchSize) {
                for (ConsumerRecord<String, String> element : buffer) {
                    minterface.dealWithData(element.value());// 处理方法应实现MethodInterface接口
                }
                consumer.commitSync();// 提交确认信息，阻塞式的更新offset
                buffer.clear();
            }
            // System.out.println("end of while");
        }
    }

    /**
     * 稍微改变功能版，不使用死循环，当获取的消息数量大于指定数量后进行处理并返回，参数minBatchSize必须大于0
     *
     * @param minterface   实现处理方法的自定义类
     * @param minBatchSize 每次最少取多少条消息
     */
    public void getMessage(MethodInterface minterface, int minBatchSize) {
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        // 直到消息数量大于指定的数量为止，不断获取消息
        while (buffer.size() < minBatchSize) {
            ConsumerRecords<String, String> records = consumer.poll(0);// 问题在这里，将该参数设置为0，可以即使返回目前可读的所有消息，设置为其他值则导致连接速度很慢且读到的消息为空
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }
        }
        for (ConsumerRecord<String, String> element : buffer) {
            minterface.dealWithData(element.value());// 处理方法应实现MethodInterface接口
        }
        consumer.commitSync();// 提交确认信息，阻塞式的更新offset
    }

    /**
     * 进一步改动功能版，不再自动进行处理和提交确认，返回消息集，处理后需要手动调用commit()提交确认信息，否则会重读同样的消息，参数必须大于0
     *
     * @param minBatchSize 每次最少取多少条消息
     * @return
     */
    public List<String> getMessage(int minBatchSize) {
        List<ConsumerRecord<String, String>> buffer = new ArrayList<>();
        List<String> values = new ArrayList<>();
        // 直到消息数量大于指定的数量为止，不断获取消息
        while (buffer.size() < minBatchSize) {
            ConsumerRecords<String, String> records = consumer.poll(0);// 问题在这里，将该参数设置为0，可以即使返回目前可读的所有消息，设置为其他值则导致连接速度很慢且读到的消息为空
            for (ConsumerRecord<String, String> record : records) {
                buffer.add(record);
            }
        }
        for (ConsumerRecord<String, String> element : buffer) {
            values.add(element.value());
        }
        return values;
    }

    /**
     * 手动调用该方法以提交最后读取的offset
     */
    public void commit() {
        consumer.commitSync();
    }

    /**
     * 获取consumer最近确认上传的offset
     *
     * @return
     */
    private long getCommittedOffset() {
        return consumer.committed(new TopicPartition(topic, 0)).offset();
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

    public static void main(String[] args) {
        KafkaHighLevelConsumer kc = new KafkaHighLevelConsumer(args[0], args[1], args[2]);
        while (true) {
            List<String> m = kc.getMessage(1);
            for (String e : m) {
                System.out.println(e);
            }
        }
    }
}

/*
 * class Deal implements MethodInterface { public Deal() {
 * 
 * }
 * 
 * public void dealWithData(String value) { System.out.println(value); } }
 */

class KafkaOffset {

    /**
     * 获取最后的offset
     *
     * @param consumer   consumer实例
     * @param topic      topic名称
     * @param partition  分区号
     * @param whichTime  使用OffsetRequest.EarliestTime()或OffsetRequest.LatestTime()
     * @param clientName consumer分组名称，与需要查询的分组名称必须相同
     * @return
     */
    private static long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime,
                                      String clientName) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
        OffsetRequest request = new OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
        OffsetResponse response = consumer.getOffsetsBefore(request);

        if (response.hasError()) {
            System.out.println("Error getting Data Offset from brokers. Reason:"
                    + ErrorMapping.exceptionNameFor(response.errorCode(topic, partition)));
            return -1;
        }
        long[] offsets = response.offsets(topic, partition);
        return offsets[0];
    }

    /**
     * 获取指定topic的leader节点（尚未测试集群情况，参数请用单节点）
     *
     * @param a_seedBrokers kafka服务器节点
     * @param a_port        端口号
     * @param a_topic       topic名称
     * @return
     */
    private static TreeMap<Integer, PartitionMetadata> findLeader(List<String> a_seedBrokers, int a_port,
                                                                  String a_topic) {
        TreeMap<Integer, PartitionMetadata> map = new TreeMap<>();
        for (String seed : a_seedBrokers) {
            SimpleConsumer consumer = null;
            consumer = new SimpleConsumer(seed, a_port, 100000, 64 * 1024, "leaderLookup" + new Date().getTime());
            List<String> topics = Collections.singletonList(a_topic);
            TopicMetadataRequest req = new TopicMetadataRequest(topics);
            TopicMetadataResponse resp = consumer.send(req);

            List<TopicMetadata> metaData = resp.topicsMetadata();
            for (TopicMetadata item : metaData) {
                for (PartitionMetadata part : item.partitionsMetadata()) {
                    map.put(part.partitionId(), part);
                }
            }
            if (consumer != null)
                consumer.close();
        }
        return map;
    }

    /**
     * 获取指定topic的最新offset
     *
     * @param topic
     * @param port
     * @param seed  kafka服务器节点地址，暂未测试集群的情况
     * @return -1表示错误，否则返回指定topic的最新
     */
    public static long getLatestOffset(String topic, String clientName, int port, String... seed) {
        List<String> seeds = new ArrayList<>();
        for (String item : seed) {
            seeds.add(item);
        }
        TreeMap<Integer, PartitionMetadata> metadatas = findLeader(seeds, port, topic);

        for (Entry<Integer, PartitionMetadata> entry : metadatas.entrySet()) {
            int partition = entry.getKey();
            String leadBroker = entry.getValue().leader().host();
            SimpleConsumer consumer = new SimpleConsumer(leadBroker, port, 100000, 64 * 1024, clientName);
            long readOffset = getLastOffset(consumer, topic, partition, kafka.api.OffsetRequest.LatestTime(),
                    clientName);
            // System.out.println("read offset:" + readOffset);
            if (consumer != null)
                consumer.close();
            return readOffset;
        }
        return -1;
    }
}