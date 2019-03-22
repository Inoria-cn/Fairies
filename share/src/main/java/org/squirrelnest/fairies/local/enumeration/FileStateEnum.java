package org.squirrelnest.fairies.local.enumeration;

import java.util.Arrays;

/**
 * Created by Inoria on 2019/3/20.
 */
public enum FileStateEnum {

    /**
     * 新通过dht得到的文件，但还未持有数据
     */
    NEW,
    /**
     * 由我提供的文件
     */
    MY_OWN_FILE,
    /**
     * 下载完成的文件
     */
    DOWNLOAD_FINISHED,
    /**
     * 下载中的文件
     */
    DOWNLOADING,
    /**
     * 未下载完成但暂停下载的文件
     */
    PAUSED;
}
