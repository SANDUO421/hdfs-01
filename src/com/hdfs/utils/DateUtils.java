package com.hdfs.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author sanduo
 * @date 2018/08/30
 */
public class DateUtils {

    /**
     * 获取当前时间,以字符串返回
     * 
     * @return
     */
    public static String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH");
        return format.format(new Date());
    }

    /**
     * 获取当前时间，以long返回
     * 
     * @return
     */
    public static Long getTime() {

        return new Date().getTime();
    }

    public static Long getTime(String parseStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH");
        long time = 0;
        try {
            time = format.parse(parseStr).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }

}
