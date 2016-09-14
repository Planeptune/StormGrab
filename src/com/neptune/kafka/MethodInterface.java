package com.neptune.kafka;

/**
 * 用于定义处理数据的方法的接口，用于实现类似将方法作为参数传递
 *
 * @author Administrator
 */
public interface MethodInterface {
    /**
     * 需要实现的处理方法
     *
     * @param value
     */
    public void dealWithData(Object value);
}