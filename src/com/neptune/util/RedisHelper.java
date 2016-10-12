package com.neptune.util;

import com.sun.istack.internal.NotNull;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by neptune on 16-10-11.
 * redis工具集
 */
public class RedisHelper {
    private Jedis jedis;

    public RedisHelper(@NotNull String host) {
        jedis = new Jedis(host);
    }

    public RedisHelper(String host, String password) {
        this(host);
        jedis.auth(password);
    }

    //重新封装，向channel中publish一条消息
    public void publish(@NotNull String message, @NotNull String[] channels) {
        for (String channel : channels)
            jedis.publish(channel, message);
    }

    //将输入的Map中的所有键值对记录
    public void record(Map<Object, Object> KeyValues) {
        for (Map.Entry entry : KeyValues.entrySet())
            jedis.set(ObjectAndBytes.objectToBytes(entry.getKey()), ObjectAndBytes.objectToBytes(entry.getValue()));
    }

    //根据输入的若干个键，返回查询到的键值对
    public Map<Object, Object> query(Object... keys) {
        Map<Object, Object> map = new HashMap();
        for (Object key : keys) {
            byte[] value = jedis.get(ObjectAndBytes.objectToBytes(key));
            if (value != null)
                map.put(key, ObjectAndBytes.bytesToObject(value));
        }
        return map;
    }

    public void close() {
        jedis.close();
    }
}
