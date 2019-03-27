package org.squirrelnest.fairies.share.dispatcher.model;

import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;

/**
 * Created by Inoria on 2019/3/22.
 */
public class SliceDownloadTarget {

    private Record targetSliceHolder;
    private Integer targetSliceIndex;

    public SliceDownloadTarget() {
    }

    public SliceDownloadTarget(Record targetSliceHolder, Integer targetSliceIndex) {
        this.targetSliceHolder = targetSliceHolder;
        this.targetSliceIndex = targetSliceIndex;
    }

    public Record getTargetSliceHolder() {
        return targetSliceHolder;
    }

    public void setTargetSliceHolder(Record targetSliceHolder) {
        this.targetSliceHolder = targetSliceHolder;
    }

    public Integer getTargetSliceIndex() {
        return targetSliceIndex;
    }

    public void setTargetSliceIndex(Integer targetSliceIndex) {
        this.targetSliceIndex = targetSliceIndex;
    }
}
