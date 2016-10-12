package com.neptune.bolt.facerig;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import com.neptune.config.facerig.PictureKey;
import com.neptune.util.HDFSHelper;
import com.neptune.util.LogWriter;

import java.io.File;
import java.util.Map;

/**
 * Created by neptune on 16-9-14.
 * 将提取的图片存入HDFS的bolt
 */
public class HDFSBolt extends BaseRichBolt {
    private static final String TAG = "hdfs-bolt";
    private String logPath;

    private OutputCollector collector;
    private TopologyContext context;
    private int id;
    private HDFSHelper hdfs;
    private String dir;

    public HDFSBolt(String hdfsDir, String logPath) {
        super();
        dir = hdfsDir;
        this.logPath = logPath;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.context = topologyContext;
        id = context.getThisTaskId();
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        //String remote = tuple.getStringByField("fileName");
        String path = tuple.getStringByField("localPath");
        String remote = (new File(path)).getName();
        Gson gson = new Gson();
        PictureKey key = (PictureKey) tuple.getValueByField("PictureKey");
        key.url = dir + File.separator + remote;
        hdfs = new HDFSHelper(null);
        File f = new File(path);
        hdfs.upload(f, key.url);
        f.delete();

        collector.emit(new Values(gson.toJson(key)));
        LogWriter.writeLog(logPath, TAG + "@" + id + ": upload to hdfs :" + key.url);

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("message"));
    }
}
