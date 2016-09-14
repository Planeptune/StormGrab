package com.neptune.tool;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-9.
 * 实现截图进程的接口
 */
public interface Grabber extends Serializable {
    public Process grab(String host, int port, String password, String url,
                     String dst, String sendTopic, String brokerList);
}
