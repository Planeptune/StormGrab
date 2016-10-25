package com.neptune;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import com.google.gson.Gson;
import com.neptune.bolt.grab.NativeGrabBolt;
import com.neptune.bolt.grab.ReduceBolt;
import com.neptune.bolt.grab.SendBolt;
import com.neptune.bolt.grab.UploadBolt;
import com.neptune.config.grab.GrabConfig;
import com.neptune.util.FileTools;
import com.neptune.util.LogWriter;
import storm.kafka.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by neptune on 16-10-25.
 */
public class GrabTopology {
    public static final String TAG = "grab-topology";
    public static final String KAFKA_SPOUT = "kafka-spout";
    public static final String REDUCE_BOLT = "reduce-bolt";
    public static final String GRAB_BOLT = "grab-bolt";
    public static final String UPLOAD_BOLT = "upload-bolt";
    public static final String KAFKA_BOLT = "kafka-bolt";
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
        LogWriter.writeLog(LOG_PATH, TAG + ":Read config from " + configFile);

        //设置kafkaSpout的选项
        BrokerHosts brokers = new ZkHosts(config.zks);
        SpoutConfig sconfig = new SpoutConfig(brokers, config.topic, config.zkRoot, config.id);
        sconfig.scheme = new SchemeAsMultiScheme(new StringScheme());
        sconfig.zkServers = Arrays.asList(config.zkServers);
        sconfig.zkPort = config.zkPort;
        sconfig.forceFromStart = false;

        //构建topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(KAFKA_SPOUT, new KafkaSpout(sconfig), config.spoutParallel);
        builder.setBolt(REDUCE_BOLT, new ReduceBolt(config.logPath + "/reduce-bolt.log"),
                config.reduceParallel).shuffleGrouping(KAFKA_SPOUT);
        builder.setBolt(GRAB_BOLT, new NativeGrabBolt(config.logPath + "/grab-bolt.log", config.libPath),
                config.grabParallel).shuffleGrouping(REDUCE_BOLT);
        builder.setBolt(UPLOAD_BOLT, new UploadBolt(config.hdfsDir, config.logPath + "/upload-bolt.log"),
                config.uploadParallel).shuffleGrouping(GRAB_BOLT);
        /*builder.setBolt(KAFKA_BOLT, new KafkaBolt<String, String>(),
                config.kafkaParallel).shuffleGrouping(UPLOAD_BOLT);*/
        builder.setBolt(KAFKA_BOLT, new SendBolt(config.brokerList, config.sendTopic, config.logPath + "/send-bolt.log"),
                config.kafkaParallel).shuffleGrouping(UPLOAD_BOLT);

        //提交topology
        Config tconfig = new Config();
        tconfig.setNumWorkers(config.workerNum);
        tconfig.setDebug(false);
        /*Properties pro = new Properties();
        pro.put("metadata.broker.list", config.brokerList);
        pro.put("producer.type", "async");
        pro.put("request.required.acks", "0");
        pro.put("serializer.class", "kafka.serializer.StringEncoder");
        tconfig.put(TridentKafkaState.KAFKA_BROKER_PROPERTIES, pro);
        tconfig.put("topic", config.sendTopic);*/
        StormSubmitter.submitTopology(args[1], tconfig, builder.createTopology());
    }
}
