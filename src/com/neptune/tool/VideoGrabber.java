package com.neptune.tool;

import com.neptune.tool.Grabber;

import java.io.IOException;
import java.util.Map;

/**
 * Created by neptune on 16-9-9.
 * 启动截图子进程
 */
public class VideoGrabber implements Grabber {
    private final static String TAG = "GrabberImpl";
    private String cmd;//指令
    private double frameRate = 1.0d;//帧率
    private String nameFormat;

    private static final String STORM_HOME = "STORM_HOME";

    @Override
    public Process grab(String host, int port, String password, String url, String dir, String sendTopic, String brokerList) {
        //启动子进程
        try {

            Map<String, String> map = System.getenv();
            String value = map.get(STORM_HOME);

            StringBuilder builder = new StringBuilder(cmd);
            builder.append(' ').append(host).append(' ').append(port).append(' ').append(password);
            builder.append(' ').append(url).append(' ').append(dir);
            builder.append(' ').append(sendTopic).append(' ').append(brokerList);
            builder.append(' ').append(frameRate);
            if (nameFormat != null)
                builder.append(' ').append(nameFormat);
            String cmd = builder.toString().replace("$" + STORM_HOME, value);

            return Runtime.getRuntime().exec(cmd, new String[]{STORM_HOME + "=" + value});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public VideoGrabber(String cmd) {
        this.cmd = cmd;
    }

    public VideoGrabber(String cmd, String format) {
        this.cmd = cmd;
        this.nameFormat = format;
    }

    public VideoGrabber(String cmd, String format, double rate) {
        this.cmd = cmd;
        this.nameFormat = format;
        this.frameRate = rate;
    }

    public void setFrameRate(double rate) {
        if (rate > 0)
            this.frameRate = rate;
    }
}
