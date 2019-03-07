package org.squirrelnest.fairies.domain.router;

import org.squirrelnest.fairies.domain.HashCode160;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 一个k-bucket，放置k个节点
 * Created by Inoria on 2019/3/7.
 */
public class Bucket {
    /**
     * bucket最多容纳的节点数
     */
    private final Integer bucketSize;

    /**
     * 备份区最多节点数
     */
    private final Integer copySize;

    private final HashCode160 localNodeId;

    private final List<Record> records;

    private final List<Record> copies;

    /**
     * 为了提高性能，使用
     */
    private Boolean empty;

    public Bucket(HashCode160 localNodeId, int bucketSize, int copySize) {
        this.localNodeId = localNodeId;
        this.bucketSize = bucketSize;
        this.copySize = copySize;
        this.records = new LinkedList<>();
        this.copies = new LinkedList<>();
    }

    public Boolean isEmpty() {
        return empty;
    }

    public List<Record> getNearNodes(HashCode160 target, Integer maxSize) {
        List<Record> sortList = new ArrayList<>(records);
        sortList.sort((r1, r2) -> {
            Integer dis1 = r1.distanceOf(target);
            Integer dis2 = r2.distanceOf(target);
            //如果距离相等，最近联系的节点应当优先级较高
            if (dis1.equals(dis2)) {
                return -1;
            }
            return dis1.compareTo(dis2);
        });
        int limit = sortList.size() < maxSize ? sortList.size() : maxSize;
        return new ArrayList<>(sortList.subList(0, limit));
    }
}
