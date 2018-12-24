package com.hdfs.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * 单例设计模式(饿汉式)<br/>
 * 读取配置文件一般是单例模式<br/>
 * 使用类的加载 器，当类的加载时就可以找到对应的配置文件加载
 * 
 * @author sanduo
 * @date 2018/08/31
 */
public class PropertyUtilsHungery {

    // 单例模式，保证获取的实例始终是一个
    private static Properties props = new Properties();// 为了保证每次获取属性时，都是单例模式;
    // 静态代码块，保证配置文件加载一次
    static {
        try {
            // 使用类的加载器加载配置文件
            // 不要要每次都加载，使用静态代码块
            props.load(PropertyUtilsHungery.class.getClassLoader().getResourceAsStream("collect.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Properties getProps() throws IOException {

        return props;
    }

}
