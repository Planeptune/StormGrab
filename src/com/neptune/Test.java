package com.neptune;

import com.google.gson.Gson;
import com.neptune.config.facerig.PictureKey;
import com.neptune.kafka.KafkaNewProducer;

/**
 * Created by neptune on 16-9-18.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        KafkaNewProducer p = new KafkaNewProducer("localhost:9092");
        PictureKey key = new PictureKey();
        key.dir = "hdfs://hadoop01:9000/test";
        key.url = "hdfs://hadoop01:9000/test/src.png";
        key.time_stamp = String.valueOf(System.currentTimeMillis());
        key.video_id = "test";
        Gson gson = new Gson();
        String json = gson.toJson(key);
        while (true) {
            p.send("test-source-topic", json);
            System.out.println(json);
            Thread.sleep(1000);
        }
    }
}
