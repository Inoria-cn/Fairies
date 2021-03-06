package org.squirrelnest.fairies.kvpairs.keyword.model;

import org.squirrelnest.fairies.domain.HashCode160;

import java.util.Map;

/**
 * 用于存储关键词列表中的文件信息
 * Created by Inoria on 2019/3/13.
 */
public class File {

    /**
     * 文件名
     */
    private String name;
    /**
     * 文件作者，可选字段，用于便于下载者辨识文件
     */
    private String author;
    /**
     * 为了防止文件名相同带来的hash重复，生成hash id的时候不能只用文件名作为参数。
     */
    private HashCode160 id;
    /**
     * 文件最近修改时间，便于查询者查找近期文件
     */
    private Long lastUpdateTime;
    /**
     * 最初的发布者设置的过期时间
     */
    private Long expireTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public HashCode160 getId() {
        return id;
    }

    public void setId(HashCode160 id) {
        this.id = id;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean valid() {
        if (id != null && name != null) {
            return true;
        }
        return false;
    }
}
