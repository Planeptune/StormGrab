package com.neptune.tool;

import com.neptune.util.LogWriter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Created by neptune on 16-9-12.
 * 未封装的redis工具，向channel发出一条消息
 */
public class VideoNotifierImpl implements IVideoNotifier {
    private final static String TAG = "PictureNotifierImpl";
    private final static String LOG_PATH = "/home/neptune/logs/redis.log";

    private Jedis mJedis;
    private String host;
    private int port;
    private String password;
    private String[] channels;

    //private FileLogger mLogger;

    public VideoNotifierImpl(String host, int port, String password, String[] channels) {
        if (host == null || password == null)
            throw new RuntimeException("Redis host or password must not be null");
        this.host = host;
        this.port = port;
        this.password = password;
        this.channels = channels;
        //mLogger = new FileLogger("redis");
    }

    //初始化Jedis
    private void initJedis() {
        if (mJedis != null)
            return;
        JedisPool pool = null;
        try {
            // JedisPool依赖于apache-commons-pools1
            JedisPoolConfig config = new JedisPoolConfig();
            pool = new JedisPool(config, host, port, 6000, password);
            mJedis = pool.getResource();
            LogWriter.writeLog(LOG_PATH, TAG + " get redis");
        } catch (Exception e) {
            LogWriter.writeLog(LOG_PATH, e.getMessage());
        }
    }

    //初始化Jedis
    public void prepare() {
        initJedis();
    }

    //获取Jedis
    public Jedis getJedis() {
        return mJedis;
    }

    //向redis发送消息，redis可以将消息推送给订阅者
    public void notify(String msg) {
        if (mJedis == null) {
            initJedis();
        }

        if (mJedis != null && channels != null) {
            for (String channel : channels) {
                try {
                    mJedis.publish(channel, msg);
                } catch (JedisConnectionException e) {
                    LogWriter.writeLog(LOG_PATH, e.getMessage());
                    stop();
                }
            }
        }
    }

    //关闭redis的连接
    public void stop() {
        if (mJedis != null) {
            mJedis.close();
            mJedis = null;
            //LogWriter.writeLog(LOG_PATH, TAG + " close redis");
        }
    }
}
