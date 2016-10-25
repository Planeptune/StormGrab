package com.neptune.bolt.grab;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.neptune.api.Grab;
import com.neptune.config.analyze.CaculateInfo;
import com.neptune.config.grab.NativeGrabCommand;
import com.neptune.util.LogWriter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by neptune on 16-10-13.
 * 新的抓帧bolt
 */
public class NativeGrabBolt extends BaseRichBolt {
    private final static String TAG = "nativeGrab-bolt";
    private String logPath;

    private OutputCollector collector;
    private TopologyContext context;
    private int id;

    private Map<Integer, Thread> threadList = new HashMap<>();
    private String libPath;

    public NativeGrabBolt(String logPath, String libPath) {
        this.logPath = logPath;
        this.libPath = libPath;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        collector = outputCollector;
        context = topologyContext;
        id = context.getThisTaskId();
        Grab.load(libPath);
        LogWriter.writeLog(logPath, TAG + "@" + id + " has load the library from : " + libPath);
        Grab.initCapture(0);
        LogWriter.writeLog(logPath, TAG + "@" + id + ": prepared");
    }

    @Override
    public void execute(Tuple tuple) {
        NativeGrabCommand cmd = (NativeGrabCommand) tuple.getValue(0);

        Thread t = threadList.get(cmd.video_id);
        if (t != null)
            LogWriter.writeLog(logPath, TAG + "@" + id + ": " + cmd.video_id + " is grabbing");
        else {
            GrabThread gt = new GrabThread(cmd.video_id, cmd.sec, collector, logPath);
            threadList.put(cmd.video_id, gt);
            gt.start();
        }

        collector.ack(tuple);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("pictureStream", "timestamp", "videoID"));
    }

    @Override
    public void cleanup() {
        super.cleanup();
        for (Map.Entry entry : threadList.entrySet()) {
            ((Thread) entry.getValue()).stop();
        }
        Grab.quitCapture();
    }
}

class GrabThread extends Thread {
    private int videoID;
    private int sec;
    private OutputCollector collector;
    private String logPath;

    public GrabThread(int videoID, int sec, OutputCollector collector, String logPath) {
        this.videoID = videoID;
        this.sec = sec;
        this.collector = collector;
        this.logPath = logPath;
    }

    //循环抓帧
    public void run() {
        while (true) {
            CaculateInfo info = new CaculateInfo();
            Grab.grabCapture(videoID, sec, info);
            if (info.pixel.length == 0)
                LogWriter.writeLog(logPath, "fail to grab a frame from : " + videoID);
            else
                collector.emit(new Values(info.pixel, String.valueOf(System.currentTimeMillis()), videoID));

            try {
                sleep(sec * 1000);
            } catch (InterruptedException e) {

            }
        }
    }
}