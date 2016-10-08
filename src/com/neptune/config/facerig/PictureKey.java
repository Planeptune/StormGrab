package com.neptune.config.facerig;

import java.io.Serializable;

/**
 * Created by neptune on 16-9-12.
 * 截取的图片信息类，用于将消息从抓帧的topology发到面部识别topology
 */
public class PictureKey implements Serializable {
    //图片存放地址
    public String url;
    //图片来源视频
    public String video_id;
    //图片时间戳
    public String time_stamp;

    public PictureKey() {

    }

    public PictureKey(String url, String video_id, String time_stamp) {
        this.url = url;
        this.video_id = video_id;
        this.time_stamp = time_stamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        PictureKey key = (PictureKey) obj;
        boolean r1 = (url == null) ? (key.url == null) : (url.equals(key.url));
        boolean r2 = (video_id == null) ? (key.video_id == null) : (video_id.equals(key.video_id));
        boolean r3 = (time_stamp == null) ? (key.time_stamp == null) : (time_stamp.equals(key.time_stamp));
        return r1 && r2 && r3;
    }

    @Override
    public int hashCode() {
        int h1 = url == null ? 0 : url.hashCode();
        int h2 = video_id == null ? 0 : video_id.hashCode();
        int h3 = time_stamp == null ? 0 : time_stamp.hashCode();
        int hashCode = h1;
        hashCode = hashCode * 31 + h2;
        hashCode = hashCode * 31 + h3;
        return hashCode;
    }

    /*public void setCodex(String codex) {
        this.codex = codex;
    }*/
}
