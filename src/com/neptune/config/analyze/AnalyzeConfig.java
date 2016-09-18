package com.neptune.config.analyze;

/**
 * Created by neptune on 16-9-13.
 * 图片分析的topology所用的配置项
 */
public class AnalyzeConfig {
    public int spoutParallel = 1;//spout并行度
    public int workerNum = 1;//工作进程数量

    public String logPath = "";//存放日志文件的目录
}
