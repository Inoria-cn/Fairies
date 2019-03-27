package org.squirrelnest.fairies.kvpairs.file.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inoria on 2019/3/13.
 */
public class FileValue {

    /**
     * 文件名
     */
    private String name;
    /**
     * 持有该文件的全部或一部分的节点的id
     */
    private List<Record> holders;
    /**
     * 该文件的所有关键字
     */
    private List<String> keywords;
    /**
     * id,由文件整体的sha1-md5组成
     */
    private HashCode160 id;
    /**
     * 文件字节数
     */
    private Integer size;
    /**
     * 分片大小
     */
    private Integer sliceSize;
    /**
     * 上次收到该value的存储消息的时间
     */
    private Long lastUpdateTime;
    /**
     * 过期时间
     * 转发不会修改该字段，主动发布时会，以保证系统中数据的时效性。
     */
    private Long expireTimestamp;

    public FileValue(String name, HashCode160 id, Integer size, Integer sliceSize) {
        this.name = name;
        this.id = id;
        this.size = size;
        this.sliceSize = sliceSize;
    }

    public FileValue(FileValue that) {
        setId(that.getId());
        setName(that.getName());
        setSize(that.getSize());
        setSliceSize(that.getSliceSize());
        setLastUpdateTime(that.getLastUpdateTime());
        setExpireTimestamp(that.getExpireTimestamp());
        setHolders(new ArrayList<>(that.getHolders()));
        setKeywords(new ArrayList<>(that.getKeywords()));
    }

    /*
     * 发布文件时，需要发布文件所有者为自己。下载文件时，如果所有者无法连接，则发送STORE消息更新所有者列表，删除无效节点并加上自己。
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Record> getHolders() {
        return holders;
    }

    public void setHolders(List<Record> holders) {
        this.holders = holders;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public HashCode160 getId() {
        return id;
    }

    public void setId(HashCode160 id) {
        this.id = id;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getSliceSize() {
        return sliceSize;
    }

    public void setSliceSize(Integer sliceSize) {
        this.sliceSize = sliceSize;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Long getExpireTimestamp() {
        return expireTimestamp;
    }

    public void setExpireTimestamp(Long expireTimestamp) {
        this.expireTimestamp = expireTimestamp;
    }


}
