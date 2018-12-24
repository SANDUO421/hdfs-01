package com.hdfs.wordcount;

/**
 * 当框架已经形成，此时只需要实现接口就行
 * 
 * @author sanduo
 * @date 2018/09/03
 */
public class IgnoreWordCountMapper implements Mapper {

    /* 忽略大小写统计
     * @see com.hdfs.wordcount.Mapper#map(java.lang.String, com.hdfs.wordcount.Context)
     */
    @Override
    public void map(String line, Context context) {
        String[] words = line.toLowerCase().split(" ");
        for (String word : words) {
            Object value = context.get(word);
            if (null == value) {
                context.write(word, 1);
            } else {
                int v = (int)value;
                context.write(word, v + 1);
            }
        }

    }

}
