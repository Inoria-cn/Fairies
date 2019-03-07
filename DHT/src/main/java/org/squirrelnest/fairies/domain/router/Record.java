package org.squirrelnest.fairies.domain.router;

import org.squirrelnest.fairies.domain.HashCode160;

/**
 * Created by Inoria on 2019/3/7.
 */
public class Record implements Comparable<Record> {
    private HashCode160 nodeId;
    private String nodeIp;
    private String nodePort;
    private Long createTimestamp;
    private Long contactTimestamp;
    private Integer secondToLive;

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

    public Long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public Long getContactTimestamp() {
        return contactTimestamp;
    }

    public void setContactTimestamp(Long contactTimestamp) {
        this.contactTimestamp = contactTimestamp;
    }

    @Override
    public int compareTo(Record record) {
        return this.getContactTimestamp().compareTo(record.getContactTimestamp());
    }

    public Integer distanceOf(HashCode160 target) {
        return this.nodeId.calculateDistance(target);
    }

    public Boolean shouldDie() {
        Long currentTimestamp = System.currentTimeMillis();
        return currentTimestamp > contactTimestamp + secondToLive;
    }
}
