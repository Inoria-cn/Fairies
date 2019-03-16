package org.squirrelnest.fairies.router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.squirrelnest.fairies.exception.GetNodeException;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.service.LocalNodeService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Kademlia 路由表实现类
 * Created by Inoria on 2019/3/7.
 */
@Component
public class RouterTable {

    @Value("${fairies.dht.k}")
    private int kSize;

    @Value("${fairies.dht.kBucketCopy}")
    private int copySize;

    @Value("${fairies.dht.maxNoResponse}")
    private int maxNoResponse;

    @Resource
    private LocalNodeService localNodeService;

    private final List<Bucket> kBuckets;

    private HashCode160 localId;

    private RouterTable() {
        kBuckets = new ArrayList<>(250);
    }

    @PostConstruct
    private void init() {
        this.localId = localNodeService.getLocalNodeId();
        for(int i = 0; i < 160; i++) {
            kBuckets.add(new Bucket(this.localId, kSize, copySize, maxNoResponse));
        }
    }

    /**
     *
     * @param target 目标节点id
     * @return 路由表中获取到的k个最近节点记录，越靠前查询优先级越高
     */
    public List<Record> getNearNodes(HashCode160 target) {
        int index = findBucketIndex(target);
        if (index < 0) {
            throw new GetNodeException("Target node is same as local node!");
        }
        int foundNodeCount = 0;
        List<Record> result = new ArrayList<>(kSize);
        for(int i = index; i >= 0; i--) {
            Bucket bucket = kBuckets.get(i);
            if(bucket.isEmpty()) {
                continue;
            }
            if(foundNodeCount >= kSize) {
                break;
            }
            int needNodeCount = kSize - foundNodeCount;
            List<Record> nodesGot = bucket.getNearNodes(target, needNodeCount);
            result.addAll(nodesGot);
            foundNodeCount += nodesGot.size();
        }
        return result;
    }

    private int findBucketIndex(HashCode160 id) {
        if (localId.equals(id)) {
            return -1;
        }
        return 159 - localId.calculateDistance(id);
    }

    /**
     * 向一个节点发送了请求后需要调用此方法
     */
    public void requestNode(HashCode160 id) {
        int index = findBucketIndex(id);
        if (index < 0) {
            return;
        }
        kBuckets.get(index).requestNode(id);
    }

    /**
     * 与一个节点发生交互后调用（dht通信中知道了此节点或者收到了该节点发来的消息）
     * @param directReceive 是不是收到了这个节点直接发来的请求或者响应消息？
     */
    public void knowNode(HashCode160 id, String ip, String port, boolean directReceive) {
        int index = findBucketIndex(id);
        if (index < 0) {
            return;
        }
        if (directReceive) {
            kBuckets.get(index).connectNode(id, ip, port);
        } else {
            kBuckets.get(index).knowNode(id, ip, port);
        }
    }

    public void knowNode(Record record, boolean directReceive) {
        knowNode(record.getNodeId(), record.getNodeIp(), record.getNodePort(), directReceive);
    }

    public void knowNodes(List<Record> records, boolean directReceive) {
        for (Record record : records) {
            knowNode(record.getNodeId(), record.getNodeIp(), record.getNodePort(), directReceive);
        }
    }
}
