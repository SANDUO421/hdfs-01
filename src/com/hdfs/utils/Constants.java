package com.hdfs.utils;

/**
 * 常量池，封装属性中的key
 * 
 * @author sanduo
 * @date 2018/08/31
 */
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
