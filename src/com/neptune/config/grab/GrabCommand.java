package com.neptune.config.grab;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-8.
 * 对视频截图的指令信息
 */
public class GrabCommand implements Serializable {
    public String url;//视频地址
    public String dir;//截图存放目录
    public String cmd;//指令

    //cmd不同取值对应的意义
    public static final String ADD = "add";
    public static final String DEL = "quit";
    public static final String PAUSE = "stop";
    public static final String CONTINUE = "start";

    public GrabCommand() {

    }

    public GrabCommand(String url, String dir, String cmd) {
        this.url = url;
        this.dir = dir;
        this.cmd = cmd;
    }
}
