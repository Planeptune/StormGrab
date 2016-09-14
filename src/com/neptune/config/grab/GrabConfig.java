package com.neptune.config.grab;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-8.
 * 截图的各类设置项
 */
public class GrabConfig implements Serializable {
    public int grabParallel = 1;//截图的Bolt并行度

    public int spoutParallel = 1;//Spout的并行度

    public int reduceParallel = 1;//还原GrabCommend对象的Bolt并行度

    public int processLimit = 1;//截图子进程上限

    public String cmd = "";//启动子进程的命令
    public String nameFormat = "frame-%05d-%d.png";//截图命名格式
    public double frameRate = 1.0;//帧率

    public String[] zkServers = {"zk01", "zk02", "zk03"};//zookeeper集群地址,格式为host1,host2...
    public int zkPort = 2181;//zookeeper端口

    public String zks = "zk01:2181,zk02:2181,zk03:2181";//kafka需要的zookeeper地址，格式为host1:port,host2:port...
    public String id = "Storm-consumer";//kafka消费者分组名称
    public String topic = "default-topic";//接受消息的topic名称
    public String zkRoot = "/storm/spout";//kafka存放消息的标识，无特别意义，可能类似于ConsumerGroup
    public String sendTopic = "default-sending-topic";//将截图发送到指定的topic
    public String brokerList = "kafka01:9092,kafka02:9092,kafka03:9092";//kafka集群地址

    public String redisHost = "develop.finalshares.com";//redis地址
    public int redisPort = 6379;//redis端口
    public String redisPassword = "";//redis密码

    public int workerNum = 1;//worker进程数量

    public String logPath = "";//日志存放目录
}
