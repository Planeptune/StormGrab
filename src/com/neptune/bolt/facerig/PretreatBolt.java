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
import com.neptune.constant.LogPath;
import com.neptune.util.ImageBase64;
import com.neptune.util.ImageHelper;
import com.neptune.util.LogWriter;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Created by neptune on 16-9-13.
 * 预处理图片并将消息还原为对象的bolt
 */
public class PretreatBolt extends BaseRichBolt {
    private static final String TAG = "pretreat-bolt";
    private static String LOG_PATH = "/pretreat-bolt.log";

    private OutputCollector collector;
    private TopologyContext context;
    private int id;

    private int height = 227;
    private int width = 227;

    public PretreatBolt(int height, int width) {
        this.height = height;
        this.width = width;
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
        String json = tuple.getString(0);
        Gson gson = new Gson();
        PictureKey key = gson.fromJson(json, PictureKey.class);

        if (key != null) {
            //将图片暂存为本地文件并改变大小
            BufferedImage img = ImageBase64.decoding(key.codex);
            if (img != null) {
                if (img.getHeight() != height || img.getWidth() != width) {
                    //裁剪图片
                    img = ImageHelper.resize(img, width, height);
                    //编码发送
                    key.setCodex(ImageBase64.encoding(img));
                    collector.emit(new Values(key));
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": Reduce command :" + json);
                } else
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": Fail to decode");
            }
        }
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("PictureKey"));
    }
}
