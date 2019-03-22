package org.squirrelnest.fairies.share.enumeration;

/**
 * 对每一个下载过程，记录下载中的不同阶段
 * Created by Inoria on 2019/3/22.
 */
public enum DownloadStateEnum {

    NEW,
    FIND_FILE_OWNER,
    FILE_OWNER_NOT_FOUND,
    CONNECT_WITH_FILE_OWNER,
    DOWNLOADING,
    PAUSED,
    FINISHED

}
