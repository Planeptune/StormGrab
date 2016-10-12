package com.neptune.bolt.analyze;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;
import com.neptune.config.analyze.AnalyzeResult;
import com.neptune.util.HBaseHelper;
import com.neptune.util.LogWriter;

import java.io.IOException;
import java.util.Map;

/**
 * Created by neptune on 16-10-9.
 * 与hbase通信的bolt，记录内容待定
 */
public class RecordBolt extends BaseRichBolt {
    private static final String TAG = "record-bolt";
    private String logPath;

    private OutputCollector collector;
    private TopologyContext context;
    private int id;

    private HBaseHelper hbase;
    private String tableName;
    private String zk;
    private int port;

    public RecordBolt(String zookeeperHost, int port, String tableName, String logPath) {
        super();
        this.logPath = logPath;
        this.tableName = tableName;
        zk = zookeeperHost;
        this.port = port;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        context = topologyContext;
        id = context.getThisTaskId();
        hbase = new HBaseHelper(zk, port);
        try {
            hbase.createTable(tableName, new String[]{"AnalyzeResult"});
        } catch (Exception e) {
            LogWriter.writeLog(logPath, TAG + "@" + id + ": " + e.getMessage());
        }
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        AnalyzeResult result = (AnalyzeResult) tuple.getValueByField("Result");
        String videoID = tuple.getStringByField("videoID");
        try {
            hbase.addRow(tableName, result.info.key, "AnalyzeResult",
                    new String[]{"videoID", "timestamp", "features"},
                    new Object[]{videoID, result.info.time_stamp, result.features});
            LogWriter.writeLog(logPath, TAG + "@" + id + ": hbase record,rowkey : " + result.info.key);
        } catch (IOException e) {
            LogWriter.writeLog(logPath, TAG + "@" + id + ": " + e.getMessage());
        }
        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public void cleanup() {
        super.cleanup();
        hbase.close();
    }
}
