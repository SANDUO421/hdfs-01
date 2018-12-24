package com.hdfs.wordcount;

/**
 * @author sanduo
 * @date 2018/09/03
 */
public class WordCountMapper implements Mapper {

    /* 
     * @see com.hdfs.wordcount.Mapper#map(java.lang.String, com.hdfs.wordcount.Context)
     */
    @Override
    public void map(String line, Context context) {

        String[] words = line.split(" ");
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
