package com.neptune.api;

import com.neptune.config.analyze.AnalyzeResult;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-18.
 */
public interface AnalyzeImpl extends Serializable {
    public AnalyzeResult append(byte[] pixel, String timestamp);

    public String analyze(byte[] pixel);
}
