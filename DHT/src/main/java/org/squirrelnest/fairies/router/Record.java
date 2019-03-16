package org.squirrelnest.fairies.router;

import org.squirrelnest.fairies.domain.HashCode160;

/**
 * Created by Inoria on 2019/3/7.
 */
public class Record implements Comparable<Record> {
    private HashCode160 nodeId;
    private String nodeIp;
    private String nodePort;
    /**
     * 最近获得该节点的响应消息的时间, 为-1表示是新加入路由表的节点，没有通信过
     */
    private Long lastReceiveTimestamp;
    /**
     * 请求未响应的次数
     */
    private Integer noResponseCount;

    public Record() {
        noResponseCount = 0;
        lastReceiveTimestamp = -1L;
    }

    public Record(HashCode160 id, String ip, String port) {
        this();
        nodeId = id;
        nodeIp = ip;
        nodePort = port;
    }

    public HashCode160 getNodeId() {
        return nodeId;
    }

    public void setNodeId(HashCode160 nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getNodePort() {
        return nodePort;
    }

    public void setNodePort(String nodePort) {
        this.nodePort = nodePort;
    }

    public Long getLastReceiveTimestamp() {
        return lastReceiveTimestamp;
    }

    public void setLastReceiveTimestamp(Long lastReceiveTimestamp) {
        this.lastReceiveTimestamp = lastReceiveTimestamp;
    }

    @Override
    public int compareTo(Record record) {
        return this.getLastReceiveTimestamp().compareTo(record.getLastReceiveTimestamp());
    }

    public Integer distanceOf(HashCode160 target) {
        return this.nodeId.calculateDistance(target);
    }

    /**
     * 每次对该记录所对应的节点发送请求时，调用此方法记录发送次数。
     */
    public void countRequest() {
        noResponseCount++;
    }

    /**
     * 请求接收到回复时，刷新未响应次数和最近通信时间
     */
    public void receivedResponse() {
        noResponseCount = 0;
        lastReceiveTimestamp = System.currentTimeMillis();
    }

    public Integer getNoResponseCount() {
        return noResponseCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Record)) return false;

        Record record = (Record) o;

        if (!getNodeId().equals(record.getNodeId())) return false;
        if (!getNodeIp().equals(record.getNodeIp())) return false;
        return getNodePort().equals(record.getNodePort());
    }

    @Override
    public int hashCode() {
        int result = getNodeId().hashCode();
        result = 31 * result + getNodeIp().hashCode();
        result = 31 * result + getNodePort().hashCode();
        return result;
    }
}
