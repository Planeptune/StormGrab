package com.neptune;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import com.google.gson.Gson;
import com.neptune.api.Analyze;
import com.neptune.bolt.analyze.AnalyzeBolt;
import com.neptune.bolt.analyze.DownloadBolt;
import com.neptune.bolt.analyze.QueryBolt;
import com.neptune.bolt.analyze.RecordBolt;
import com.neptune.config.analyze.AnalyzeConfig;
import com.neptune.util.FileTools;
import com.neptune.util.LogWriter;
import storm.kafka.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by neptune on 16-9-18.
 * 创建并提交人脸识别的topology
 */
public class AnalyzeTopology {
    private static final String TAG = "analyze-topology";
    private static String LOG_PATH = "/analyze-topology.log";

    private static final String KAFKA_SPOUT = "kafka-spout";
    private static final String DOWNLOAD_BOLT = "download-bolt";
    private static final String ANALYZE_BOLT = "analyze-bolt";
    private static final String QUERY_BOLT = "query-bolt";
    private static final String RECORD_BOLT = "record-bolt";

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        AnalyzeConfig config;
        try {
            String json = FileTools.read(new File(args[0]));
            Gson gson = new Gson();
            config = gson.fromJson(json, AnalyzeConfig.class);
        } catch (IOException e) {
            LogWriter.writeLog(LOG_PATH, TAG + ": fail to load config from :" + args[0]);
            return;
        }

        if (config == null)
            return;

        //设置日志文件路径
        LOG_PATH = config.logPath + "/analyze-topology.log";
        LogWriter.writeLog(LOG_PATH, TAG + ": load config from :" + args[0]);

        //设置人脸识别缓冲区大小和等待时间
        Analyze.bufferLimit = config.bufferLimit;
        Analyze.timeLimit = config.timeLimit;

        //配置kafkaspout
        BrokerHosts brokers = new ZkHosts(config.zks);
        SpoutConfig conf = new SpoutConfig(brokers, config.topic, config.zkRoot, config.id);
        conf.scheme = new SchemeAsMultiScheme(new StringScheme());
        conf.zkServers = Arrays.asList(config.zkServers);
        conf.zkPort = config.zkPort;
        conf.forceFromStart = false;

        //创建topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(KAFKA_SPOUT, new KafkaSpout(conf), config.spoutParallel);
        builder.setBolt(DOWNLOAD_BOLT, new DownloadBolt(config.logPath + "/download-bolt.log"),
                config.downloadParallel).shuffleGrouping(KAFKA_SPOUT);
        builder.setBolt(ANALYZE_BOLT, new AnalyzeBolt(config.analyzeLibPath, config.logPath + "/analyze-bolt.log"),
                config.analyzeParallel).shuffleGrouping(DOWNLOAD_BOLT);
        builder.setBolt(QUERY_BOLT, new QueryBolt(config.redisHost, config.redisPassword, config.redisChannels,
                        config.recognizeLibPath, config.logPath + "/query-bolt.log"),
                config.queryParallel).shuffleGrouping(ANALYZE_BOLT);
        builder.setBolt(RECORD_BOLT, new RecordBolt(config.zks.split(":")[0], Integer.valueOf(config.zks.split(":")[1]), config.tableName, config.logPath + "/record-bolt.log"),
                config.recordParallel).shuffleGrouping(QUERY_BOLT);

        //提交topology
        Config tconfig = new Config();
        tconfig.setNumWorkers(config.workerNum);
        tconfig.setDebug(false);
        StormSubmitter.submitTopology(args[1], tconfig, builder.createTopology());
    }
}
