package com.neptune;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import com.google.gson.Gson;
import com.neptune.bolt.grab.GrabBolt;
import com.neptune.bolt.grab.ReduceBolt;
import com.neptune.config.grab.GrabConfig;
import com.neptune.tool.Grabber;
import com.neptune.tool.VideoGrabber;
import com.neptune.util.FileTools;
import com.neptune.util.LogWriter;
import storm.kafka.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by neptune on 16-9-9.
 * 程序入口，生成并提交topology
 */
public class GrabTopology {
    public static final String TAG = "grab-topology";
    public static final String KAFKA_SPOUT = "kafka-spout";
    public static final String REDUCE_BOLT = "reduce-bolt";
    public static final String GRAB_BOLT = "grab-bolt";
    private static String LOG_PATH = "/grab-topology.log";

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        String configFile = "grabber_config.json";
        if (args.length > 0)
            configFile = args[0];

        //从json文件获取配置
        File cFile = new File(configFile);
        Gson gson = new Gson();
        GrabConfig config = null;
        try {
            config = gson.fromJson(FileTools.read(cFile), GrabConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (config == null) {
            LogWriter.writeLog(LOG_PATH, TAG + ":Fail to read json");
            return;
        }

        //设置日志文件路径
        LOG_PATH = config.logPath + "/grab-topology.log";
        GrabThread.logPath = config.logPath + "/grab-thread.log";
        LogWriter.writeLog(LOG_PATH, TAG + ":Read config from " + configFile);

        //设置kafkaSpout的选项
        BrokerHosts brokerHosts = new ZkHosts(config.zks);
        SpoutConfig sconfig = new SpoutConfig(brokerHosts, config.topic, config.zkRoot, config.id);
        sconfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        sconfig.zkServers = Arrays.asList(config.zkServers);
        sconfig.zkPort = config.zkPort;
        sconfig.forceFromStart = false;

        //截图进程
        Grabber grabber = new VideoGrabber(config.cmd, config.nameFormat, config.frameRate);

        //构建topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(KAFKA_SPOUT, new KafkaSpout(sconfig), config.spoutParallel);
        builder.setBolt(REDUCE_BOLT, new ReduceBolt(config.logPath + "/reduce-bolt.log"), config.reduceParallel).shuffleGrouping(KAFKA_SPOUT);
        GrabBolt grabBolt = new GrabBolt(grabber, config.processLimit / config.grabParallel, config.sendTopic, config.brokerList, config.logPath + "/grab-bolt.log");
        grabBolt.setRedis(config.redisHost, config.redisPort, config.redisPassword);
        builder.setBolt(GRAB_BOLT, grabBolt, config.grabParallel).fieldsGrouping(REDUCE_BOLT, new Fields("command"));

        //提交topology
        Config tconfig = new Config();
        tconfig.setNumWorkers(config.workerNum);
        tconfig.setDebug(false);
        StormSubmitter.submitTopology(args[1], tconfig, builder.createTopology());
    }
}
