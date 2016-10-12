package com.neptune.api;

import com.neptune.config.analyze.AnalyzeResult;
import com.neptune.config.analyze.CaculateInfo;

import java.util.*;

/**
 * Created by neptune on 16-9-18.
 * 人脸识别接口
 */
public class Analyze {
    private static List<CaculateInfo> list = new ArrayList<>();
    public static int bufferLimit = 0;//缓冲区上限
    public static int timeLimit = 1000;//等待时间上限
    private static long lastTime = -1;

    static {
        //System.loadLibrary("");
    }

    //压入缓冲区，如果缓冲区满则进行识别并返回识别结果，否则返回null
    public static List<AnalyzeResult> append(CaculateInfo info) {
        list.add(info);
        if (lastTime == -1)
            lastTime = System.currentTimeMillis();
        //缓冲区已满
        if (list.size() >= bufferLimit || System.currentTimeMillis() - lastTime >= timeLimit) {
            Map<String, float[]> map = analyze(list);
            List<AnalyzeResult> result = new ArrayList<>();
            Map<String, CaculateInfo> temp = new HashMap<>();

            //使用临时的哈希表保存CaculateInfo，方便之后进行查找
            for (CaculateInfo e : list) {
                temp.put(e.key, e);
            }

            //根据返回的每条记录中的key，从哈希表中寻找对应的时间戳，并生成AnalyzeInfo
            for (Map.Entry<String, float[]> entry : map.entrySet()) {
                AnalyzeResult re = new AnalyzeResult();
                //re.key = entry.getKey();
                re.features = entry.getValue();
                re.info = temp.get(entry.getKey());
                result.add(re);
            }
            list.clear();
            lastTime = System.currentTimeMillis();
            return result;
        } else {
            lastTime = System.currentTimeMillis();
            return null;
        }
    }

    /*private static Map<String, Float> analyze(List<CaculateInfo> infos) {
        Map<String, Float> map = new HashMap<>();
        Random ra = new Random();
        for (CaculateInfo info : infos) {
            float s = ra.nextFloat();
            map.put(info.key, s);
        }
        return map;
    }*/

    //本地方法，输入一个CaculateInfo的列表，返回每个文件地址与特征值的哈希表
    private static native Map<String, float[]> analyze(List<CaculateInfo> infos);

    @Deprecated
    public static void main(String[] args) {

    }
}