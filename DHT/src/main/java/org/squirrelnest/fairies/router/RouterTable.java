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
    private int bucketSize;

    @Value("${fairies.dht.kBucketCopy}")
    private int copySize;

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
            kBuckets.add(new Bucket(this.localId, bucketSize, copySize));
        }
    }

    public List<Record> getNearNodes(HashCode160 target, Integer limit) {
        if (localId.equals(target)) {
            throw new GetNodeException("Target node is same as local node!");
        }
        int index = 159 - localId.calculateDistance(target);
        int foundNodeCount = 0;
        List<Record> result = new ArrayList<>(limit);
        for(int i = index; i >= 0; i--) {
            Bucket bucket = kBuckets.get(i);
            if(bucket.isEmpty()) {
                continue;
            }
            if(foundNodeCount >= limit) {
                break;
            }
            int needNodeCount = limit - foundNodeCount;
            List<Record> nodesGot = bucket.getNearNodes(target, needNodeCount);
            result.addAll(nodesGot);
            foundNodeCount += nodesGot.size();
        }
        return result;
    }
}
