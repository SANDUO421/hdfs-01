package com.hdfs.wordcount;

import java.util.HashMap;

/**
 * 定义一个上下文
 * 
 * @author sanduo
 * @date 2018/09/03
 */
public class Context {

    private HashMap<Object, Object> contextMap = new HashMap<Object, Object>();

    public void write(Object key, Object value) {
        contextMap.put(key, value);
    }

    public Object get(Object key) {
        return contextMap.get(key);
    }

    public HashMap<Object, Object> getContextMap() {
        return contextMap;
    }
}
