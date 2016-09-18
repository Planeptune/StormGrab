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
import com.neptune.config.facerig.CaculatePicture;
import com.neptune.config.facerig.PictureKey;
import com.neptune.constant.LogPath;
import com.neptune.util.LogWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
        LOG_PATH = LogPath.FPATH + "/pretreat-bolt.log";
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        context = topologyContext;
        id = context.getThisTaskId();
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": prepared!");
    }

    @Override
    public void execute(Tuple tuple) {
        CaculatePicture cal = (CaculatePicture) tuple.getValueByField("CaculateInfo");
        PictureKey key = (PictureKey) tuple.getValueByField("PictureKey");

        //将图片进行人脸分离
        List<String> paths = facerig.facerig(cal);
        int i = 0;
        for (String path : paths) {
            collector.emit(new Values(String.valueOf(System.currentTimeMillis()) + "@" + i + ".png", path, key));
        }

        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": seperate image at :" + cal.url);

        //collector.emit(new Values(list));

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("fileName", "localPath", "PictureKey"));
    }
}
