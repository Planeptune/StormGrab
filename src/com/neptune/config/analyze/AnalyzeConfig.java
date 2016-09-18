package com.neptune.config.analyze;

/**
 * Created by neptune on 16-9-13.
 * 图片分析的topology所用的配置项
 */
public class AnalyzeConfig {
    public int spoutParallel = 1;//spout并行度
    public int downloadParallel = 1;//下载图片的bolt并行度
    public int analyzeParallel = 1;//人脸识别的并行度
    public int workerNum = 1;//工作进程数量

    public String logPath = "";//存放日志文件的目录

    public int bufferLimit = 0;//图片处理缓存的数量

    public String zks = "localhost:2181";//kafka需要的zookeeper地址
    public String id = "analyze-consumer";//kafka消费者分组名称
    public String zkRoot = "";//kafka存放消息的标识，根据zks的末尾设置
    public String[] zkServers = {"localhost"};//zookeeper集群地址
    public int zkPort = 2181;//zookeeper端口
    public String topic = "test-source-topic";//消息来源的topic
}
