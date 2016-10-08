package com.neptune;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import com.google.gson.Gson;
import com.neptune.bolt.facerig.FacerigBolt;
import com.neptune.bolt.facerig.HDFSBolt;
import com.neptune.bolt.facerig.PretreatBolt;
import com.neptune.config.facerig.FacerigConfig;
import com.neptune.util.FileTools;
import com.neptune.util.LogWriter;
import storm.kafka.*;
import storm.kafka.bolt.KafkaBolt;
import storm.kafka.trident.TridentKafkaState;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by neptune on 16-9-13.
 * 创建并提交用于人脸分离的topology
 */
public class FacerigTopology {
    private static final String TAG = "facerig-topology";
    private static String LOG_PATH = "/facerig-topology.log";

    public static final String KAFKA_SPOUT = "kafka-spout";
    public static final String PRETREAT_BOLT = "pretreat-bolt";
    public static final String FACERIG_BOLT = "facerig-bolt";
    public static final String KAFKA_BOLT = "kafka-bolt";
    public static final String HDFS_BOLT = "hdfs-bolt";

    /**
     * args[0]:配置文件路径
     * args[1]:topology名称
     *
     * @param args
     */
    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        //加载配置文件
        String confPath = args[0];
        FacerigConfig config = null;
        try {
            Gson gson = new Gson();
            String json = FileTools.read(new File(confPath));
            config = gson.fromJson(json, FacerigConfig.class);
        } catch (IOException e) {
            LogWriter.writeLog(LOG_PATH, TAG + ": Fail to load config from " + confPath);
        }

        if (config == null)
            return;

        //设置日志文件路径
        LOG_PATH = config.logPath + "/facerig-topology.log";
        LogWriter.writeLog(LOG_PATH, TAG + ": read config from :" + confPath);

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
        builder.setBolt(PRETREAT_BOLT, new PretreatBolt(config.height, config.width, config.logPath + "/pretreat-bolt.log"),
                config.pretreatParallel).shuffleGrouping(KAFKA_SPOUT);
        builder.setBolt(FACERIG_BOLT, new FacerigBolt(config.logPath + "/facerig-bolt.log"),
                config.facerigParallel).shuffleGrouping(PRETREAT_BOLT);
        builder.setBolt(HDFS_BOLT, new HDFSBolt(config.hdfsDir, config.logPath + "/hdfs-bolt.log"), config.hdfsParallel).shuffleGrouping(FACERIG_BOLT);
        builder.setBolt(KAFKA_BOLT, new KafkaBolt<String, String>(),
                config.kafkaParallel).shuffleGrouping(HDFS_BOLT);

        //提交topology
        Config tconfig = new Config();
        tconfig.setNumWorkers(config.workerNum);
        tconfig.setDebug(false);
        Properties pro = new Properties();
        pro.put("metadata.broker.list", config.bootstrap);
        pro.put("producer.type", "async");
        pro.put("request.required.acks", "0");
        pro.put("serializer.class", "kafka.serializer.StringEncoder");
        tconfig.put(TridentKafkaState.KAFKA_BROKER_PROPERTIES, pro);
        tconfig.put("topic", config.targetTopic);
        StormSubmitter.submitTopology(args[1], tconfig, builder.createTopology());
    }
}
