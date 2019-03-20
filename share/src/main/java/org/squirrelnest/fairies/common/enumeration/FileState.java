package org.squirrelnest.fairies.common.enumeration;

/**
 * Created by Inoria on 2019/3/20.
 */
public enum FileState {

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
    PAUSED

}
