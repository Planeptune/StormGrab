package com.neptune.bolt.grab;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import com.neptune.config.facerig.PictureKey;
import com.neptune.kafka.KafkaNewProducer;
import com.neptune.util.HDFSHelper;
import com.neptune.util.LogWriter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Map;

/**
 * Created by neptune on 16-10-20.
 * 将图片上传hdfs的bolt
 */
public class UploadBolt extends BaseRichBolt {
    private static final String TAG = "upload-bolt";
    private String logPath;

    private TopologyContext context;
    private OutputCollector collector;
    private int id;

    private String hdfsDir;
    private HDFSHelper hdfs;

    public UploadBolt(String hdfsDir, String logPath) {
        this.hdfsDir = hdfsDir;
        this.logPath = logPath;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        context = topologyContext;
        collector = outputCollector;
        id = context.getThisTaskId();
        hdfs = new HDFSHelper(null);
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        byte[] image = (byte[]) tuple.getValueByField("pictureStream");
        int vedioID = tuple.getIntegerByField("videoID");
        String timestamp = tuple.getStringByField("timestamp");
        Gson gson = new Gson();
        ByteArrayInputStream in = new ByteArrayInputStream(image);

        hdfs.upload(in, hdfsDir + File.separator + timestamp + "_" + id + ".png");
        PictureKey key = new PictureKey();
        key.url = hdfsDir + File.separator + timestamp + "_" + id + ".png";
        key.video_id = String.valueOf(vedioID);
        key.time_stamp = timestamp;
        String json = gson.toJson(key);

        collector.emit(new Values(json));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("message"));
    }
}
