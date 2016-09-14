package com.neptune.config.analyze;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-14.
 * 图片分析的结果
 */
public class AnalyzeResult implements Serializable {
    String id;//人员姓名
    String timestamp;//图片时间戳
    float features;//特征值
}
