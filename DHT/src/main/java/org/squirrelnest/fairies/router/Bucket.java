package org.squirrelnest.fairies.router;

import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.exception.InvalidStateException;
import org.squirrelnest.fairies.router.collection.BucketNodes;

import java.util.*;

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

    private final BucketNodes records;

    /**
     * 最近一次对该bucket中的节点发送网络请求的时间
     */
    private Long lastRequestTimestamp;

    public Bucket(HashCode160 localNodeId, int bucketSize, int copySize, int maxNoResponse) {
        this.localNodeId = localNodeId;
        this.bucketSize = bucketSize;
        this.copySize = copySize;
        this.records = new BucketNodes(bucketSize, copySize, maxNoResponse);
    }

    public Boolean isEmpty() {
        return records.size() == 0;
    }


    public List<Record> getNearNodes(HashCode160 target, Integer maxSize) {
        if (isEmpty()) {
            return new ArrayList<>(2);
        }
        List<Record> sortList = records.getClonedRecords();
        sortList.sort((r1, r2) -> {
            Integer dis1 = r1.distanceOf(target);
            Integer dis2 = r2.distanceOf(target);
            //如果距离相等，按未响应次数排列，然后按最近联系的顺序排列
            if (dis1.equals(dis2)) {
                Integer noResponse1 = r1.getNoResponseCount();
                Integer noResponse2 = r2.getNoResponseCount();
                if (noResponse1.equals(noResponse2)) {
                    return -1;
                }
                return noResponse1.compareTo(noResponse2);
            }
            return dis1.compareTo(dis2);
        });
        int limit = sortList.size() < maxSize ? sortList.size() : maxSize;
        return new ArrayList<>(sortList.subList(0, limit));
    }

    /**
     * 向节点发送请求的时候需要调用此方法
     */
    public void requestNode(HashCode160 id) {
        Record record = records.find(id);
        if (record == null) {
            return;
        }
        this.lastRequestTimestamp = System.currentTimeMillis();
        record.countRequest();
    }

    /*
     路由表节点更新逻辑说明：
     系统知道了一个新节点： 节点是否已经存在于bucket中 / 节点是别人告诉的还是收到了它的消息
     收到了该节点的消息且节点记录已经存在：为该节点赋予最大优先级，未响应数清零
     收到了该节点的消息且节点记录不存在：新建节点，未响应数清零，作为重要节点添加进bucket
     从别人处听说了节点且记录已经存在：什么都不做
     从别人处听说了节点且记录不存在：新建节点，作为普通节点添加进bucket
    */

    /**
     * 从一个节点收到消息时调用
     * @param id 节点id
     */
    public void connectNode(HashCode160 id, String ip, String port) {
        if (records.recordExists(id)) {
            records.find(id).receivedResponse();
            records.priorityMax(id);
        } else {
            Record record = new Record(id, ip, port);
            record.receivedResponse();
            records.addImportantRecord(record);
        }
    }

    /**
     * 通过其他节点知道了一个新节点，但是没有与该节点发生直接通讯时调用
     */
    public void knowNode(HashCode160 id, String ip, String port) {
        if (records.recordExists(id)) {
            //
        } else {
            Record record = new Record(id, ip, port);
            records.addNormalRecord(record);
        }
    }

    public Long getLastRequestTimestamp() {
        return lastRequestTimestamp;
    }
}