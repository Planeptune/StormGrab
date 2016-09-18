package com.neptune.bolt.analyze;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.neptune.api.Analyze;
import com.neptune.config.analyze.AnalyzeResult;
import com.neptune.config.analyze.CaculateInfo;
import com.neptune.constant.LogPath;
import com.neptune.util.LogWriter;

import java.util.List;
import java.util.Map;

/**
 * Created by neptune on 16-9-18.
 * 人脸识别的bolt
 */
public class AnalyzeBolt extends BaseRichBolt {
    private static final String TAG = "analyze-bolt";
    private static String LOG_PATH = "/analyze-bolt.log";

    private OutputCollector collector;
    private TopologyContext context;
    private int id;

    public AnalyzeBolt() {
        super();
        LOG_PATH = LogPath.APATH + "/analyze-bolt.log";
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        context = topologyContext;
        id = context.getThisTaskId();
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        CaculateInfo info = (CaculateInfo) tuple.getValue(0);
        List<AnalyzeResult> list = Analyze.append(info);
        if (list == null) {
            LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": append image at :" + info.key);
        } else {
            //TODO 对识别结果的后续处理
        }
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
