package com.neptune.api;

import com.neptune.config.analyze.CaculateInfo;

/**
 * Created by neptune on 16-10-10.
 * 图片抓取，具体实现由库实现
 */
public class Grab {

    //必须先执行该方法才能使用其他方法
    public static void load(String libPath) {
        if(libPath.contains(".so"))
        {
            System.load(libPath);
        }
        else {
            System.loadLibrary(libPath);
        }
    }

    //本地方法，初始化抓帧，参数暂时不用
    public static native void initCapture(int mode);

    //本地方法，抓帧程序，返回结果存在info中
    public static native void grabCapture(int video_id, int sec, CaculateInfo info);

    //本地方法，结束抓帧
    public static native void quitCapture();

    public static void main(String[] args) {

    }
}
