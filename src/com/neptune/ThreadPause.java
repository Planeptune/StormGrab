package com.neptune;

/**
 * Created by neptune on 16-10-12.
 * 很有启发意义的线程暂停小例子
 */
public class ThreadPause extends Thread {
    Object o = new Object();

    public void run() {
        synchronized (o) {
            System.out.println("第一次输出");
            try {
                System.out.println("即将暂停");
                o.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("这行输出看不到");
        }
        System.out.println("这行输出看不到");
    }

    public static void main(String[] args) {
        ThreadPause t = new ThreadPause();
        t.start();

        try {
            sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (t.o) {
            t.o.notify();
        }
    }
}
