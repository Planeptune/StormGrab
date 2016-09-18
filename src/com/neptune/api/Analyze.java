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

    //压入缓冲区，如果缓冲区满则进行识别并返回识别结果，否则返回null
    public static List<AnalyzeResult> append(CaculateInfo info) {
        list.add(info);
        //缓冲区已满
        if (list.size() >= bufferLimit) {
            Map<String, Float> map = analyze(list);
            List<AnalyzeResult> result = new ArrayList<>();
            Map<String, CaculateInfo> temp = new HashMap<>();

            //使用临时的哈希表保存CaculateInfo，方便之后进行查找
            for (CaculateInfo e : list) {
                temp.put(e.key, e);
            }

            //根据返回的每条记录中的key，从哈希表中寻找对应的时间戳，并生成AnalyzeInfo
            for (Map.Entry<String, Float> entry : map.entrySet()) {
                AnalyzeResult re = new AnalyzeResult();
                re.key = entry.getKey();
                re.features = entry.getValue();
                re.timestamp = temp.get(re.key).time_stamp;
                result.add(re);
            }
            list.clear();
            return result;
        } else
            return null;
    }

    //TODO 调用外部库
    private static Map<String, Float> analyze(List<CaculateInfo> infos) {
        Map<String, Float> map = new HashMap<>();
        Random ra = new Random();
        for (CaculateInfo info : infos) {
            float s = ra.nextFloat();
            map.put(info.key, s);
        }
        return map;
    }
}
