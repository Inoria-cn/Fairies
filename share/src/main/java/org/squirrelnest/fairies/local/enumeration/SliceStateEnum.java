package org.squirrelnest.fairies.local.enumeration;

import java.util.Arrays;

/**
 * Created by Inoria on 2019/3/21.
 */
public enum SliceStateEnum {
    /**
     * 分片未拥有 - 下载中
     */
    DOWNLOADING,
    /**
     * 分片未拥有 - 已经找到持有者但没有开始下载
     */
    LACK_AND_FOUND,
    /**
     * 分片未拥有 - 暂时没有找到持有者
     */
    LACK_AND_NOT_FOUND,
    /**
     * 分片部分拥有
     */
    PARTLY,
    /**
     * 分片已拥有
     */
    HAVING;

    @SuppressWarnings("all")
    public boolean haveSlice() {
        return Arrays.asList(
                HAVING
        ).contains(this);
    }

}
