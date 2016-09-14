package com.neptune.util;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by neptune on 16-9-9.
 * 使用标准输入输出流向子进程发送消息
 */
public class ProcessHelper {
    public static void sendMessage(Process process, String msg) {
        if (process == null)
            return;
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.println(msg);
        writer.flush();
        writer.close();
    }
}
