package com.hdfs.demo01;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.junit.Before;
import org.junit.Test;

/**
 * hdfs 入门程序：客户端上传 访问
 * 
 * @author sanduo
 * @date 2018/08/24
 */
public class HdfsClientDemo {

    private FileSystem fs = null;

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

    @Before
    public void init() throws Exception {
        Configuration conf = new Configuration();
        conf.set("dfs.replication", "2");
        conf.set("dfs.blocksize", "64m");
        fs = FileSystem.get(new URI("hdfs://hadoop01:9000"), conf, "root");
    }

    /**
     * 上传文件
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testadd() throws IllegalArgumentException, IOException {
        fs.copyFromLocalFile(new Path("G:/people-copy.xml"), new Path("/"));
        fs.close();
    }

    /**
     * 测试get:从hdfs上下载文件到本地<br/>
     * 需要配置环境
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testGet() throws IllegalArgumentException, IOException {
        fs.copyToLocalFile(new Path("/hadoop-2.8.3.tar.gz"), new Path("f:/"));
        fs.close();
    }

    /**
     * 修改名称，移动路径（但是注意：移动的文件夹一定要存在）名称
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testRename() throws IllegalArgumentException, IOException {
        fs.rename(new Path("/people-copy.xml"), new Path("/aaa/people.xml"));
        fs.close();
    }

    /**
     * 创建文件夹
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testMkdir() throws IllegalArgumentException, IOException {
        fs.mkdirs(new Path("/aaa"));
        fs.close();
    }

    /**
     * 删除文件夹
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testRm() throws IllegalArgumentException, IOException {
        fs.delete(new Path("/aaa"), true);
        fs.close();
    }

    /**
     * 查询hdfs指定目录下的文件
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testFileList() throws IllegalArgumentException, IOException {
        // 只查询文件的信息不查询文件夹的信息
        RemoteIterator<LocatedFileStatus> listFiles = fs.listFiles(new Path("/"), true);
        while (listFiles.hasNext()) {
            LocatedFileStatus status = listFiles.next();
            System.out.println("文件路径:" + status.getPath());
            System.out.println("文件大小：" + status.getBlockSize());
            System.out.println("文件的长度:" + status.getLen());
            System.out.println("拥有者:" + status.getOwner());
            System.out.println("文件副本数:" + status.getReplication());
            System.out.println("文件块信息:" + Arrays.toString(status.getBlockLocations()));
            System.out.println("-----------------------");
        }
        fs.close();
    }

    /**
     * 查询hdfs指定目录下的文件和文件夹的信息
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testDirInfo() throws IllegalArgumentException, IOException {
        // 查询文件夹信息和文件信息
        FileStatus[] listStatus = fs.listStatus(new Path("/"));
        for (FileStatus status : listStatus) {
            System.out.println("文件路径:" + status.getPath());
            System.out.println(status.isDirectory() ? "这是文件夹" : "这是文件");
            System.out.println("文件大小：" + status.getBlockSize());
            System.out.println("文件的长度:" + status.getLen());
            System.out.println("拥有者:" + status.getOwner());
            System.out.println("文件副本数:" + status.getReplication());
            System.out.println("-----------------------");
        }
        fs.close();
    }

    /**
     * 读取Hdfs中的内容
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
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

    /**
     * 读取Hdfs中指定偏移量的内容
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */
    @Test
    public void testRandomReadHdfsData() throws IllegalArgumentException, IOException {

        FSDataInputStream in = fs.open(new Path("/text.txt"));

        // 第三种方式：从文件的某一个位置开始读

        // 定位
        in.seek(50);
        byte[] buf = new byte[49];
        in.read(buf);
        System.out.println(new String(buf, Charset.defaultCharset()));

        in.close();
        fs.close();
    }

    /**
     * 往hdfs中文件写内容
     * 
     * @throws IOException
     * @throws IllegalArgumentException
     */

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

}
