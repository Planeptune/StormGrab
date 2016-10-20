package com.neptune;

import com.neptune.api.Grab;

/**
 * Created by neptune on 16-10-20.
 */
public class Test {
    public static void main(String[] args) {
        Grab.load("/home/neptune/下载/libgrab.so");
        System.out.println("load");
        Grab.initCapture(0);
        System.out.println("init");
        Grab.quitCapture();
        System.out.println("quit");
    }
}
