package com.neptune.tool;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-12.
 * 作用待探究
 */
public interface IVideoNotifier extends Serializable {
    void prepare();

    void notify(String msg);

    void stop();
}
