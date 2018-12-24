package com.hdfs.wordcount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

/**
 * 框架式开发
 * 
 * @author sanduo
 * @date 2018/09/03
 */
public class HdfsWordCount {
    private static Properties props = null;
    private static Path INPUT_PATH = null;
    private static Path OUTPUT_PATH = null;

    public static void main(String[] args) throws Exception {
        // 加载配置实现类
        Mapper mapper = loadProperties();
        // 构造context
        Context context = new Context();

        // 构造一个客户端
        FileSystem fs = FileSystem.get(new URI("hdfs://hadoop01:9000"), new Configuration(), "root");
        // 处理数据-获取需要读取的文件
        RemoteIterator<LocatedFileStatus> iter = fs.listFiles(INPUT_PATH, true);
        while (iter.hasNext()) {
            LocatedFileStatus file = iter.next();

            // 获取输入流
            FSDataInputStream in = fs.open(file.getPath());
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = null;
            // 逐行读取文件
            while ((line = br.readLine()) != null) {
                // 调用一个方法对每一行进行业务处理（面向接口编程：加载反射配置文件获取实现类）

                // 解析每行数据，放到context中
                mapper.map(line, context);
            }
            br.close();
            in.close();
        }

        // 输出结果---处理context

        HashMap<Object, Object> contextMap = context.getContextMap();
        // 判断输出目录是否存在
        // Path outPath = new Path("/wordcount/output");
        if (!fs.exists(OUTPUT_PATH)) {
            fs.mkdirs(OUTPUT_PATH);
        }

        // 打开输出流
        FSDataOutputStream out = fs.create(new Path(OUTPUT_PATH, new Path("res.dat")));
        for (Entry<Object, Object> entry : contextMap.entrySet()) {
            out.write((entry.getKey() + "\t" + entry.getValue() + "\r\n").getBytes());
        }
        out.close();
        fs.close();

        System.out.println("数据统计完成");
        // 将这一行的处理结果放入一个缓存

        // 调用一个方法将缓存中的结果数据输出到HDFS的结果文件
    }

    /**
     * 加载配置类
     * 
     * @param <T>
     */
    private static Mapper loadProperties() {
        props = new Properties();
        try {
            props.load(HdfsWordCount.class.getClassLoader().getResourceAsStream("job.properties"));
            INPUT_PATH = new Path(props.getProperty("INPUT_PATH"));
            OUTPUT_PATH = new Path(props.getProperty("OUTPUT_PATH"));
            Class<?> mapper_class = Class.forName(props.getProperty("MAPPER_CLASS"));
            Mapper mapper = (Mapper)mapper_class.newInstance();
            return mapper;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
