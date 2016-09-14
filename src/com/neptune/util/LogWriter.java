package com.neptune.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by neptune on 16-9-9.
 * 用于写入日志文件的工具
 */
public class LogWriter {
    public static void writeLog(String filePath, String log) {
        String path = filePath;
        //文件名为空则使用默认值
        if (filePath == null)
            path = "default_log.log";

        File f = new File(path);
        if (!f.exists())
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        try {
            FileWriter fw = new FileWriter(f, true);

            //将日志加上格式化的信息头，输出时间信息
            StringBuffer sb = new StringBuffer();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            sb.append("[" + String.format("%04d/%02d/%02d ", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE)) + " " + String.format("%02d:%02d:%02d ", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)) + "]");

            fw.append(sb.toString() + log + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
