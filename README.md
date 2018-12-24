
# 大数据 -大数据基本API+日志采集清理系统


## hdfs：安装、部署、java测试

```
package com.hdfs.demo01;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * hdfs 入门程序：客户端上传
 * 
 * @author sanduo
 * @date 2018/08/24
 */
public class HdfsClientDemo {
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        // 覆盖hdfs-default.xml 配置
        Configuration conf = new Configuration();
        // hdfs需要保存的副本数 2
        conf.set("dfs.replication", "2");
        // hdfs 的切块大小
        conf.set("dfs.blocksize", "64m");
        // 获取hdfs客户端---复制本地文件到hdfs集群
        FileSystem fs = FileSystem.get(new URI("hdfs://hadoop01:9000"), conf, "root");

        fs.copyFromLocalFile(new Path("F:/java-学习/Java-部署文档/03-代码规范/p3c-master.zip"), new Path("/aaa"));

    }

}

```

### 实战（数据收集定时）：


#### 需求

---
在业务系统的服务器上，业务程序会不断生成业务日志（比如网站的页面访问日志）
业务日志是用log4j生成的，会不断地切出日志文件
需要定期（比如每小时）从业务服务器上的日志目录中，探测需要采集的日志文件(access.log不能采)，发往HDFS

**注意**：业务服务器可能有多台(hdfs上的文件名不能直接用日志服务器上的文件名)
当天采集到的日志要放在hdfs的当天目录中
采集完成的日志文件，需要移动到到日志服务器的一个备份目录中
定期检查（一小时检查一次）备份目录，将备份时长超出24小时的日志文件清除
---


#### 分析


1.  流程

启动一个定时任务：<br/>
&nbsp;&nbsp;-定时探测日志源目录
&nbsp;&nbsp;-获取需要采集的日志文件
&nbsp;&nbsp;-移动日志文件到一个待上传的临时目录
&nbsp;&nbsp;-遍历待上传目录中的文件，逐个传输到hdfs的目标路径，同时将传输传输完成的文件移动到备份目录

启动一个定时任务：<br/>
&nbsp;&nbsp;-探测备份目录中的备份数据，检查是否超出备份时长，如果超出，则删除

2.  路径规划

日志源路径：E:/bigdata/logs/accesslog
带上传临时目录：E:/bigdata/logs/toupload
备份目录：E:/bigdata/logs/backup/日期

HDFS存储路径：/logs/日期
HDFS中文件的前缀：accress_log_
HDFS中文件的后缀：.log

#### 创建文件

**注意**

创建文件，存放文件 <br/>
E:\bigdata\logs\accesslog

### 实战1（数据清理）

增强健壮性（判断）、可维护性（常量设计）、安全性（单例）

增强：
*	重试机制
*	记录故障节点

还需要考虑：连接不上，重试的设计，比如发送给运维人员邮件或者信息；
hdfs 挂掉：记录上一次文件传输的偏移量

#### 单例设计模式

1.	单例-饿汉式

```
public class PropertyUtilsHungery {

    // 单例模式，保证获取的实例始终是一个
    private static Properties props = new Properties();// 为了保证每次获取属性时，都是单例模式;
    // 静态代码块，保证配置文件加载一次
    static {
        try {
            // 使用类的加载器加载配置文件
            // 不要要每次都加载，使用静态代码块
            props.load(PropertyUtilsHungery.class.getResourceAsStream("collection.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Properties getProps() throws IOException {

        return props;
    }

}
```

2.	单例-懒汉式(资源考虑和线程安全考虑)

```
public class PropertyUtilsLazy {

    // 单例模式，保证获取的实例始终是一个
    private static Properties props = null;

    public static Properties getProps() throws IOException {
        // 双重加锁保证只实例化一次
        if (props == null) {
            synchronized (PropertyUtilsLazy.class) {
                if (props == null) {
                    props = new Properties();
                    props.load(PropertyUtilsLazy.class.getResourceAsStream("collection.properties"));
                }
            }
        }
        return props;
    }

}

```


#### 配置文件读取设计(编译检查，防止硬编码)，


1.	将常量信息以键值对的相识配置到.properties 文件中

```
#本地日志目录
LOG_SOURCE_DIR=E:/bigdata/logs/accesslog/   
LOG_TOUPLOAD_DIR=E:/bigdata/logs/toupload/   
LOG_BACKUP_BASE_DIR=E:/bigdata/logs/backup/
LOG_BACKUP_TIMEOUT=24
LOG_LEGAL_PREFIX=accress.log.

#HDFS的配置
HDFS_URI=hdfs://hadoop:9000/
HDFS_DEST_BASE_DIR=/logs/
HDFS_FILE_PREFIX=accress_log_
HDFS_FILE_SUFFIX=.log
```

2.	将常量key以类的形式加载，进行编译检查

```
public class Constants {
    // 约束key
    /**
     * 日志上传目录可以
     */
    public static final String LOG_SOURCE_DIR = "LOG_SOURCE_DIR";
    /**
     * 日志上传临时目录可以
     */
    public static final String LOG_TOUPLOAD_DIR = "LOG_TOUPLOAD_DIR";
    /**
     * 备份目录key
     */
    public static final String LOG_BACKUP_BASE_DIR = "LOG_BACKUP_BASE_DIR";
    /**
     * 备份文件的超时时间
     */
    public static final String LOG_BACKUP_TIMEOUT = "LOG_BACKUP_TIMEOUT";
    /**
     * 上传文件的前缀
     */
    public static final String LOG_LEGAL_PREFIX = "LOG_LEGAL_PREFIX";
    /**
     * hdfs的namenode的地址
     */
    public static final String HDFS_URI = "HDFS_URI";
    /**
     * hdfs的存放目录
     */
    public static final String HDFS_DEST_BASE_DIR = "HDFS_DEST_BASE_DIR";
    /**
     * hdfs存放文件的前缀
     */
    public static final String HDFS_FILE_PREFIX = "HDFS_FILE_PREFIX";
    /**
     * hdfs存放文件的后缀
     */
    public static final String HDFS_FILE_SUFFIX = "HDFS_FILE_SUFFIX";

}

```

#### 读取hdfs文件和上传文件到hdfs

```读取
//读取
@Test
    public void testReadHdfsFile() throws IllegalArgumentException, IOException {

        FSDataInputStream in = fs.open(new Path("/text.txt"));

        FileOutputStream out = new FileOutputStream("G:/问情诗.txt");
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));

        // 第一种方法：字节流读取
        /* byte[] by = new byte[1024];
        int len = 0;
        while ((len = in.read(by)) != -1) {
            System.out.write(by, 0, len);
        }*/
        // 第二种方法：缓冲流包装字符流，字符流包装字节流
        BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()));
        String line = null;
        while ((line = br.readLine()) != null) {
            // System.out.println(line);
            writer.write(line);
            writer.write("\r\n");
            writer.flush();
        }
        // 第三种方式：从文件的某一个位置开始读
        br.close();
        in.close();
        writer.close();
        out.close();
        fs.close();
    }
```

```上传
//上传
@Test
    public void testWriteHdfsData() throws IllegalArgumentException, IOException {
        // 输出流
        FSDataOutputStream out = fs.create(new Path("/yyy.gif"), false);

        // G:\0_1310541840FfCT.gif
        // 本地输入流---因为是图片，只能用字节流
        FileInputStream in = new FileInputStream("G:\\0_1310541840FfCT.gif");

        // 读取
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        fs.close();
    }
```
### 实战2（框架式开发）

#### HDFS 版本的wordCount程序实现

#####分析

---

1.	去hdfs 中读取文件，一次读取一行

2.	调用一个方法对每一行进行业务处理

3.	将这一行的处理结果放入一个缓存

4.	调用一个方法将缓存中的结果数据输出到HDFS的结果文件

--- 


##### 面向接口编程

```
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

```


### HDFS工作机制-namenode元数据管理


#### 实战


##### Mapreduce 实现wordcount设计思路

---
1.	map task 划分一行数据，分词
2.	shuffle  数据分发（分组）
3.	reduce task 按照组取数据统计
4.	将统计解决存储到hdfs中
---











