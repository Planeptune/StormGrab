package com.neptune;

import com.google.gson.Gson;
import com.neptune.config.facerig.PictureKey;
import com.neptune.kafka.KafkaNewProducer;

/**
 * Created by neptune on 16-10-8.
 */
public class FacerigTest {
    public static void main(String[] args) throws Exception {
        KafkaNewProducer p = new KafkaNewProducer("localhost:9092");
        Gson gson = new Gson();
        PictureKey key = new PictureKey();
        key.url = "hdfs://hadoop01:9000/test/src.png";
        key.video_id = "test";
        //key.time_stamp = String.valueOf(System.currentTimeMillis());

        while (true) {
            key.time_stamp = String.valueOf(System.currentTimeMillis());
            p.send("source-topic", gson.toJson(key));
            System.out.println(gson.toJson(key));
            Thread.sleep(500);
        }
    }
}
