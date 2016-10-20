package com.neptune.bolt.analyze;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.neptune.api.Recognize;
import com.neptune.config.analyze.AnalyzeResult;
import com.neptune.tool.IVideoNotifier;
import com.neptune.tool.VideoNotifierImpl;
import com.neptune.util.LogWriter;
import com.neptune.util.RedisHelper;

import java.util.Map;

/**
 * Created by neptune on 16-10-9.
 * 查询是否是黑名单
 */
public class QueryBolt extends BaseRichBolt {
    private static final String TAG = "query-bolt";
    private String logPath;

    private int id;
    private TopologyContext context;
    private OutputCollector collector;

    private RedisHelper redis;
    private String host;
    private String password;
    private String[] channels;

    public QueryBolt(String host, String password, String[] channels, String logPath) {
        super();
        this.logPath = logPath;
        this.host = host;
        this.password = password;
        this.channels = channels;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        context = topologyContext;
        collector = outputCollector;
        id = context.getThisTaskId();
        redis = new RedisHelper(host, password);
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        AnalyzeResult result = (AnalyzeResult) tuple.getValueByField("AnalyzeResult");
        String videoID = tuple.getStringByField("videoID");
        int id;

        id = Recognize.recognize(result.features);
        if (id != -1) {
            redis.publish("Find people in blacklist,ID: " + id, channels);
        }
        collector.emit(new Values(result, videoID));
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("Result", "videoID"));
    }

    @Override
    public void cleanup() {
        super.cleanup();
        redis.close();
    }
}
