package com.neptune.util;

import java.io.*;

/**
 * Created by neptune on 16-9-9.
 * 处理文件的工具集
 */
public class FileTools {
    /**
     * 从JSON文件中读取字符串
     *
     * @param f JSON文件
     * @return 字符串
     * @throws IOException
     */
    public static String read(File f) throws IOException {
        FileInputStream is = new FileInputStream(f);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String str;

        while ((str = br.readLine()) != null)
            sb.append(str);
        return sb.toString();
    }

    public static boolean write(File f, String str) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(str);
            bw.flush();
            bw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
