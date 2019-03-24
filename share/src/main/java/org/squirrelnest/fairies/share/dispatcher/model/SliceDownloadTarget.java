package org.squirrelnest.fairies.share.dispatcher.model;

import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;

/**
 * Created by Inoria on 2019/3/22.
 */
public class SliceDownloadTarget {

    private Record targetSliceHolder;
    private HashCode160 targetFileId;
    private Integer targetSliceIndex;

    public SliceDownloadTarget() {
    }

    public SliceDownloadTarget(Record targetSliceHolder, HashCode160 targetFileId, Integer targetSliceIndex) {
        this.targetSliceHolder = targetSliceHolder;
        this.targetFileId = targetFileId;
        this.targetSliceIndex = targetSliceIndex;
    }

    public Record getTargetSliceHolder() {
        return targetSliceHolder;
    }

    public void setTargetSliceHolder(Record targetSliceHolder) {
        this.targetSliceHolder = targetSliceHolder;
    }

    public HashCode160 getTargetFileId() {
        return targetFileId;
    }

    public void setTargetFileId(HashCode160 targetFileId) {
        this.targetFileId = targetFileId;
    }

    public Integer getTargetSliceIndex() {
        return targetSliceIndex;
    }

    public void setTargetSliceIndex(Integer targetSliceIndex) {
        this.targetSliceIndex = targetSliceIndex;
    }
}
