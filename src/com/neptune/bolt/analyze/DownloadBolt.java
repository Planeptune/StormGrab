package com.neptune.bolt.analyze;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import com.neptune.config.analyze.CaculateInfo;
import com.neptune.config.facerig.PictureKey;
import com.neptune.util.HDFSHelper;
import com.neptune.util.LogWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Created by neptune on 16-9-18.
 * 将人脸文件从HDFS上下载的bolt
 */
public class DownloadBolt extends BaseRichBolt {
    private String logPath;
    private static final String TAG = "dowload-bolt";

    private OutputCollector collector;
    private TopologyContext context;
    private int id;

    private HDFSHelper hdfs;

    public DownloadBolt(String logPath) {
        super();
        this.logPath = logPath;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        context = topologyContext;
        collector = outputCollector;
        id = context.getThisTaskId();
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        String json = tuple.getString(0);
        Gson gson = new Gson();
        PictureKey key = gson.fromJson(json, PictureKey.class);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        hdfs = new HDFSHelper(null);
        if (hdfs.download(os, key.url)) {
            try {
                //读取图片
                ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
                BufferedImage img = ImageIO.read(in);
                byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
                CaculateInfo info = new CaculateInfo(key.url, pixels, img.getWidth(), img.getHeight(), key.time_stamp);
                collector.emit(new Values(info, key.video_id));
                LogWriter.writeLog(logPath, TAG + "@" + id + ": download image from :" + key.url);
            } catch (IOException e) {
                LogWriter.writeLog(logPath, TAG + "@" + id + ": " + e.getMessage());
            }
        } else {
            LogWriter.writeLog(logPath, TAG + "@" + id + ": fail to download :" + key.url);
        }
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("CaculateInfo", "videoID"));
    }
}
