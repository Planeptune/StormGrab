package com.neptune.bolt.facerig;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.neptune.config.facerig.PictureKey;
import com.neptune.constant.LogPath;
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
    private static String LOG_PATH = "/hdfs-bolt";

    private OutputCollector collector;
    private TopologyContext context;
    private int id;
    private HDFSHelper hdfs;

    public HDFSBolt() {
        super();
        LOG_PATH = LogPath.FPATH + "/hdfs-bolt.log";
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.collector = outputCollector;
        this.context = topologyContext;
        id = context.getThisTaskId();
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        String remote = tuple.getStringByField("fileName");
        String path = tuple.getStringByField("localPath");
        PictureKey key = (PictureKey) tuple.getValueByField("PictureKey");
        key.url = key.dir + File.separator + remote;
        hdfs = new HDFSHelper(key.dir);
        File f = new File(path);
        hdfs.upload(f, remote);
        f.delete();

        collector.emit(new Values(key));
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": upload to hdfs :" + key.dir);

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("message"));
    }
}
