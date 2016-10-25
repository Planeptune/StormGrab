package com.neptune.bolt.grab;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.neptune.kafka.KafkaNewProducer;
import com.neptune.util.LogWriter;

import java.util.Map;

/**
 * Created by neptune on 16-10-25.
 */
public class SendBolt extends BaseRichBolt {

    private static final String TAG = "send-bolt";
    private String logPath;

    private TopologyContext context;
    private OutputCollector collector;
    private int id;

    private KafkaNewProducer producer;
    private String bootstrap;
    private String topic;

    public SendBolt(String bootstrap, String topic, String logPath) {
        this.topic = topic;
        this.logPath = logPath;
        this.bootstrap = bootstrap;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        context = topologyContext;
        collector = outputCollector;
        id = context.getThisTaskId();
        producer = new KafkaNewProducer(bootstrap);
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        String msg = tuple.getString(0);
        if (msg != null)
            try {
                producer.send(topic, msg);
            } catch (Exception e) {
                LogWriter.writeLog(logPath, TAG + "@" + id + ": " + e.getMessage());
            }
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
