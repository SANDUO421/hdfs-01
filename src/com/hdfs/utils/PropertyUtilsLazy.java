package com.hdfs.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * 单例设计模式(懒汉式:双重加锁，考虑线程安全)<br/>
 * 解决的问题：需要时加载而不是一次性加载<br/>
 * 读取配置文件一般是单例模式<br/>
 * 使用类的加载 器，当类的加载时就可以找到对应的配置文件加载
 * 
 * @author sanduo
 * @date 2018/08/31
 */
public class PropertyUtilsLazy {

    // 单例模式，保证获取的实例始终是一个
    private volatile static Properties props = null;

    public static Properties getProps() throws IOException {
        // 双重加锁保证只实例化一次
        if (null == props) {
            synchronized (PropertyUtilsLazy.class) {
                if (props == null) {
                    props = new Properties();
                    props.load(PropertyUtilsLazy.class.getClassLoader().getResourceAsStream("collect.properties"));
                }
            }
        }

        return props;
    }

}
