package com.neptune.config.analyze;

import com.google.gson.Gson;
import com.neptune.util.FileTools;

import java.io.File;

/**
 * Created by neptune on 16-9-13.
 * 图片分析的topology所用的配置项
 */
public class AnalyzeConfig {
    public int spoutParallel = 1;//spout并行度
    public int downloadParallel = 1;//下载图片的bolt并行度
    public int analyzeParallel = 1;//人脸识别的并行度
    public int queryParallel = 1;//查询bolt的并行度
    public int recordParallel = 1;//记录bolt的并行度
    public int workerNum = 1;//工作进程数量

    public String logPath = "";//存放日志文件的目录

    public int bufferLimit = 0;//图片处理缓存的数量
    public int timeLimit = 1000;//图片处理缓存等待时间

    public String zks = "localhost:2181";//zookeeper地址与端口，不能使用ip
    public String id = "analyze-consumer";//kafka消费者分组名称
    public String zkRoot = "";//kafka存放消息的标识，根据zks的末尾设置
    public String[] zkServers = {"localhost"};//zookeeper集群地址
    public int zkPort = 2181;//zookeeper端口
    public String topic = "test-source-topic";//消息来源的topic
    public String redisHost = "localhost";//redis地址
    public String redisPassword = "";//redis密码
    public String[] redisChannels = {};//redis发布的channel名称
    public String tableName = "default-table";//记录数据的hbase表名
}
