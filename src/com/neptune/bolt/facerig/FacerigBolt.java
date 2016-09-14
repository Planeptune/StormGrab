package com.neptune.bolt.facerig;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import com.neptune.api.FacerigImpl;
import com.neptune.config.facerig.PictureKey;
import com.neptune.constant.LogPath;
import com.neptune.util.ImageBase64;
import com.neptune.util.LogWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by neptune on 16-9-13.
 */
public class FacerigBolt extends BaseRichBolt {
    private static final String TAG = "facerig-bolt";
    private static String LOG_PATH = "/pretreat-bolt.log";

    private OutputCollector collector;
    private TopologyContext context;
    private int id;

    private FacerigImpl facerig;//人脸分离接口

    public FacerigBolt(FacerigImpl facerig) {
        this.facerig = facerig;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        context = topologyContext;
        id = context.getThisTaskId();
        LOG_PATH = LogPath.FPATH + "/pretreat-bolt.log";
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": prepared!");
    }

    @Override
    public void execute(Tuple tuple) {
        PictureKey key = (PictureKey) tuple.getValue(0);

        //将图片解码并进行人脸分离
        BufferedImage img = ImageBase64.decoding(key.codex);
        List<String> faces = facerig.facerig(img);
        //List<PictureKey> list = new ArrayList<>();
        Gson gson = new Gson();

        for (String face : faces) {
            key.setCodex(face);
            //list.add(key);
            collector.emit(new Values(gson.toJson(key)));
        }

        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": seperate image at :" + key.url + " from :" + key.video_id);

        //collector.emit(new Values(list));

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("message"));
    }
}
