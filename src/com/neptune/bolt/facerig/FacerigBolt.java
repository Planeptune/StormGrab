package com.neptune.bolt.facerig;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.neptune.api.Facerig;
import com.neptune.config.analyze.CaculateInfo;
import com.neptune.config.facerig.PictureKey;
import com.neptune.util.LogWriter;

import java.util.List;
import java.util.Map;

/**
 * Created by neptune on 16-9-13.
 * 人脸分离的bolt
 */
public class FacerigBolt extends BaseRichBolt {
    private static final String TAG = "facerig-bolt";
    private String logPath;

    private OutputCollector collector;
    private TopologyContext context;
    private int id;

    private String libPath;//so文件的绝对路径

    public FacerigBolt(String libPath, String logPath) {
        super();
        this.logPath = logPath;
        this.libPath = libPath;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        context = topologyContext;
        id = context.getThisTaskId();
        Facerig.load(libPath);
        LogWriter.writeLog(logPath, TAG + "@" + id + ": load library from: " + libPath);
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared!");
    }

    @Override
    public void execute(Tuple tuple) {
        CaculateInfo cal = (CaculateInfo) tuple.getValueByField("CaculateInfo");
        PictureKey key = (PictureKey) tuple.getValueByField("PictureKey");

        //将图片进行人脸分离
        List<String> paths = Facerig.facerig(cal);
        for (String path : paths) {
            collector.emit(new Values(path, key));
        }

        LogWriter.writeLog(logPath, TAG + "@" + id + ": seperate image at :" + cal.key);

        //collector.emit(new Values(list));

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("localPath", "PictureKey"));
    }
}
