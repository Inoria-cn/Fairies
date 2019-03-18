package org.squirrelnest.fairies.kvpairs.file.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.squirrelnest.fairies.domain.HashCode160;

import java.util.List;

/**
 * Created by Inoria on 2019/3/13.
 */
public class FileValue {

    private String name;
    private List<HashCode160> holders;
    private List<String> keywords;
    private HashCode160 lastSourceNodeId;
    private Long lastUpdateTime;
    //转发不会修改该字段，主动发布时会，以保证系统中数据的时效性。
    private Long expireTimestamp;

    /*
     * 发布文件时，需要发布文件所有者为自己。下载文件时，如果所有者无法连接，则发送STORE消息更新所有者列表，删除无效节点并加上自己。
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<HashCode160> getHolders() {
        return holders;
    }

    public void setHolders(List<HashCode160> holders) {
        this.holders = holders;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public HashCode160 getLastSourceNodeId() {
        return lastSourceNodeId;
    }

    public void setLastSourceNodeId(HashCode160 lastSourceNodeId) {
        this.lastSourceNodeId = lastSourceNodeId;
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
