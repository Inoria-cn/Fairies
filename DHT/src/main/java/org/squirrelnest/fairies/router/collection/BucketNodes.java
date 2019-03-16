package org.squirrelnest.fairies.router.collection;

import com.sun.org.apache.regexp.internal.RE;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.exception.InvalidStateException;
import org.squirrelnest.fairies.router.Record;
import org.squirrelnest.fairies.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Inoria on 2019/3/16.
 */
public class BucketNodes {

    private List<Record> container;
    private final int maxSize;
    private final Integer tolerableMaxNoResponseCount;

    public BucketNodes(int recordMaxSize, int backupMaxSize, int tolerableMaxNoResponseCount) {
        this.maxSize = recordMaxSize + backupMaxSize;
        this.tolerableMaxNoResponseCount = tolerableMaxNoResponseCount;
        int insuranceSize = (int)(this.maxSize * 1.5);
        container = new ArrayList<Record>(insuranceSize);
    }

    public synchronized void addNormalRecord(Record record) {
        if (container.size() < maxSize) {
            container.add(0, record);
        } else {
            for (int i = 0, length = container.size(); i < length; i++) {
                if (container.get(i).getNoResponseCount() > tolerableMaxNoResponseCount) {
                    CollectionUtils.replace(container, i, record);
                    return;
                }
            }
        }
    }

    public synchronized void addImportantRecord(Record record) {
        if (container.size() < maxSize) {
            container.add(record);
            return;
        }

        //已经达到最大节点数
        for (int i = 0, length = container.size(); i < length; i++) {
            if (container.get(i).getNoResponseCount() > tolerableMaxNoResponseCount) {
                container.remove(i);
                container.add(record);
                return;
            }
        }

        //没有失效节点，丢弃该节点
    }

    public boolean recordExists(HashCode160 id) {
        for (Record record : container) {
            if (record.getNodeId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void priorityMax(HashCode160 id) {
        int index = findIndexById(container, id);
        if (index < 0) {
            throw new InvalidStateException();
        }
        Record record = container.get(index);
        container.add(record);
        container.remove(index);
    }

    public Record find(HashCode160 id) {
        int index = findIndexById(container, id);
        if (index < 0) {
            return null;
        }
        return container.get(index);
    }

    public int size() {
        return container.size();
    }

    private int findIndexById(List<Record> list, HashCode160 id) {
        for(int i = 0, length = list.size(); i < length; i++) {
            Record current = list.get(i);
            if (current.getNodeId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public ArrayList<Record> getClonedRecords() {
        return new ArrayList<>(this.container);
    }
}
