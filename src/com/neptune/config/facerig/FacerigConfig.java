package com.neptune.config.facerig;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-13.
 * 用于面部分离的topology的配置项
 */
public class FacerigConfig implements Serializable {

    public String logPath = "";//日志文件存放目录

    public int spoutParallel = 1;//spout的并行度
    public int pretreatParallel = 1;//预处理图片的bolt的并行度
    public int facerigParallel = 1;//人脸分离的bolt的并行度
    public int hdfsParallel = 1;//hdfs写入的并行度
    public int kafkaParallel = 1;//发送消息的bolt的并行度

    public String targetTopic = "facerig-topic";//kafka消息发送的目的topic
    public String bootstrap = "localhost:9092";//kafka集群地址
    public String id = "facerig-consumer";//kafka消费者分组名称
    public String zkRoot = "";//kafka存放消息的标识
    public String zks = "localhost:2181";//kafka需要的zookeeper集群地址
    public String[] zkServers = {"localhost"};//zookeeper集群地址
    public int zkPort = 2181;//zookeeper端口
    public String topic = "default-topic";//消息来源的topic

    public String hdfsDir = "hdfs://localhost:9000/";//存放人脸图片的hdfs目录

    public int height = 227;//图片高度
    public int width = 227;//图片宽度

    public String libPath = "/usr/lib/libfacerig.so";//人脸提取的库文件绝对路径
    public String modelPath="";//model文件夹所在的路径

    public int workerNum = 1;//工作进程数量
}
