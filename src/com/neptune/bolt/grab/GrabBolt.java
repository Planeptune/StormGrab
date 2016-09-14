package com.neptune.bolt.grab;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.neptune.config.grab.GrabCommand;
import com.neptune.constant.LogPath;
import com.neptune.tool.Grabber;
import com.neptune.util.LogWriter;
import com.neptune.util.ProcessHelper;
import org.apache.commons.collections.map.HashedMap;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by neptune on 16-9-9.
 * 用于截图的bolt，为每一个收到的url启动一个子进程截图
 */
public class GrabBolt implements IRichBolt {
    private static final String TAG = "grab-bolt";
    private static String LOG_PATH = "/grab-bolt.log";

    private int id;

    private GrabCommand cmd;
    private TopologyContext context;
    private OutputCollector collector;
    private int processAmount = 0;//子进程数量
    private int processLimit;//子进程数量限制

    private Grabber grabber;
    private String sendTopic;
    private String brokerList;

    //redis信息
    private String redisHost;
    private int redisPort;
    private String redisPassword;

    private Map<String, Process> processMap;//存放url与对应的抓取进程的哈希表

    public GrabBolt(Grabber grabber, int grabLimit, String sendTopic, String brokerList) {
        this.grabber = grabber;
        processLimit = grabLimit;
        this.sendTopic = sendTopic;
        this.brokerList = brokerList;
    }

    public void setRedis(String host, int port, String password) {
        this.redisHost = host;
        this.redisPort = port;
        this.redisPassword = password;
    }

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        context = topologyContext;
        collector = outputCollector;
        id = context.getThisTaskId();
        processMap = new HashedMap();
        LOG_PATH = LogPath.PATH + "/grab-bolt.log";
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ": prepeared");
    }

    @Override
    public void execute(Tuple tuple) {
        cmd = (GrabCommand) tuple.getValue(0);

        if (cmd == null)
            return;
        Process process = null;
        LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Receive command:" + cmd.url + "," + cmd.dir + "," + cmd.cmd);

        switch (cmd.cmd) {
            //命令为add
            case GrabCommand.ADD: {
                process = processMap.get(cmd.url);
                if (process != null) {
                    try {
                        //检查该进程是否还在运行中
                        boolean hasExit = process.waitFor(-1, TimeUnit.SECONDS);
                        if (!hasExit) {
                            LogWriter.writeLog("/grab-bolt.log", TAG + "@" + id + ":" + cmd.url + " has been grabbing");
                            break;
                        } else {
                            processMap.remove(cmd.url);
                            processAmount--;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                //启动进程
                if (processAmount >= processLimit) {
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Process limit!");
                    break;
                }
                process = grabber.grab(redisHost, redisPort, redisPassword,
                        cmd.url, cmd.dir, sendTopic, brokerList);
                if (process != null) {
                    processMap.put(cmd.url, process);
                    processAmount++;
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Add process for " + cmd.url);
                } else {
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Fail to start process for " + cmd.url);
                }
                break;
            }
            //命令为DEL
            case GrabCommand.DEL: {
                if (processMap.get(cmd.url) != null) {
                    processMap.get(cmd.url).destroy();
                    processMap.remove(cmd.url);
                    processAmount--;
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Delete process for " + cmd.url);
                    break;
                } else {
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":No process for " + cmd.url);
                    break;
                }
            }
            //命令为PAUSE
            case GrabCommand.PAUSE: {
                if (processMap.get(cmd.url) != null) {
                    ProcessHelper.sendMessage(processMap.get(cmd.url), GrabCommand.PAUSE);
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Process pause at " + cmd.url);
                    break;
                } else {
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":No process for " + cmd.url);
                    break;
                }
            }
            //命令为CONTINUE
            case GrabCommand.CONTINUE: {
                if (processMap.get(cmd.url) != null) {
                    ProcessHelper.sendMessage(processMap.get(cmd.url), GrabCommand.CONTINUE);
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Process continue for " + cmd.url);
                    break;
                } else {
                    LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":No process for " + cmd.url);
                    break;
                }
            }
            default:
                LogWriter.writeLog(LOG_PATH, TAG + "@" + id + ":Unknown command");
        }
        collector.ack(tuple);
    }

    @Override
    public void cleanup() {
        //关闭所有进程
        Collection<Process> processes = processMap.values();
        processes.stream().filter(Process::isAlive).forEach(Process::destroy);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
