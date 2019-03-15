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
    //一个kv对最初创建的时候进行设置，以后的转发不会修改该字段，以保证系统中数据的时效性。
    private Long expireTimestamp;

    private static final String NODE_ID_FIELD_NAME = "lastSourceNodeId";

    @Override
    public String toString() {
        JSONObject rawJSONObject = JSON.parseObject(JSON.toJSONString(this));
        rawJSONObject.put(NODE_ID_FIELD_NAME, lastSourceNodeId.toString());
        return '1' + rawJSONObject.toJSONString();
    }

    /**
     * 发布文件时，需要发布文件所有者为自己。下载文件时，如果所有者无法连接，则发送STORE消息更新所有者列表，删除无效节点并加上自己。
     * @param raw 带有特定前缀的序列化文件消息
     * @return 反序列化后构建的此类对象
     */
    public static FileValue parseString(String raw) {
        JSONObject rawJSONObject = JSON.parseObject(raw);
        String lastNodeIdString = rawJSONObject.getString(NODE_ID_FIELD_NAME);
        rawJSONObject.put(NODE_ID_FIELD_NAME, null);
        FileValue result = JSON.parseObject(rawJSONObject.toJSONString(), FileValue.class);
        result.setLastSourceNodeId(HashCode160.parseString(lastNodeIdString));
        return result;
    }

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
