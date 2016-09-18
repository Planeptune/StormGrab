package com.neptune;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.topology.TopologyBuilder;
import com.google.gson.Gson;
import com.neptune.bolt.analyze.AnalyzeBolt;
import com.neptune.bolt.analyze.DownloadBolt;
import com.neptune.config.analyze.AnalyzeConfig;
import com.neptune.constant.LogPath;
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

    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        AnalyzeConfig config = null;
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

        LogPath.APATH = config.logPath;
        LOG_PATH = LogPath.APATH + "/analyze-topology.log";
        LogWriter.writeLog(LOG_PATH, TAG + ": load config from :" + args[0]);

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
        builder.setBolt(DOWNLOAD_BOLT, new DownloadBolt(), config.downloadParallel).shuffleGrouping(KAFKA_SPOUT);
        builder.setBolt(ANALYZE_BOLT, new AnalyzeBolt(), config.analyzeParallel).shuffleGrouping(DOWNLOAD_BOLT);

        //提交topology
        Config tconfig = new Config();
        tconfig.setNumWorkers(config.workerNum);
        tconfig.setDebug(false);
        StormSubmitter.submitTopology(args[1], tconfig, builder.createTopology());
    }
}
