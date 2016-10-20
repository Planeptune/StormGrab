package com.neptune.bolt.grab;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import com.neptune.config.grab.GrabCommand;
import com.neptune.config.grab.NativeGrabCommand;
import com.neptune.util.LogWriter;

import java.util.Map;

/**
 * Created by neptune on 16-9-8.
 * 用于将JSON还原为GrabCommand对象的Bolt
 */
public class ReduceBolt extends BaseRichBolt {
    private static final String TAG = "reduce-bolt";
    private String logPath;

    //private GrabCommand cmd;
    private OutputCollector collector;
    private TopologyContext context;

    private int id;

    public ReduceBolt(String logPath) {
        super();
        this.logPath = logPath;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.context = topologyContext;
        this.collector = outputCollector;
        id = context.getThisTaskId();
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        String json = tuple.getString(0);
        Gson gson = new Gson();

        NativeGrabCommand cmd = gson.fromJson(json, NativeGrabCommand.class);

        if (cmd != null) {
            collector.emit(new Values(cmd));
            LogWriter.writeLog(logPath, TAG + "@" + id + ":Reduce command :" + json);
        }

        collector.ack(tuple);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("Command"));
    }
}
