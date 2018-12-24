package com.hdfs.wordcount;

/**
 * 定义一个通用的接口
 * 
 * @author sanduo
 * @date 2018/09/03
 */
public interface Mapper {

    /**
     * 切分每一个数据，并做处理
     */
    public void map(String line, Context context);
}
