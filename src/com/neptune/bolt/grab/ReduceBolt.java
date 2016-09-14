package com.neptune.bolt.grab;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.google.gson.Gson;
import com.neptune.config.grab.GrabCommand;
import com.neptune.constant.LogPath;
import com.neptune.util.LogWriter;

import java.util.Map;

/**
 * Created by neptune on 16-9-8.
 * 用于将JSON还原为GrabCommand对象的Bolt
 */
public class ReduceBolt implements IRichBolt {
    private static final String TAG = "reduce-bolt";
    private static String LOG_PATH = "/reduce-bolt.log";

    private GrabCommand cmd;
    private OutputCollector collector;
    private TopologyContext context;

    private int id;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.context = topologyContext;
        this.collector = outputCollector;
        id = context.getThisTaskId();
        LOG_PATH = LogPath.PATH + "/reduce-bolt.log";
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        String json = tuple.getString(0);
        Gson gson = new Gson();

        cmd = gson.fromJson(json, GrabCommand.class);

        if (cmd != null) {
            collector.emit(new Values(cmd));
            LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Reduce command :" + json);
        }

        collector.ack(tuple);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("command"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
