package org.squirrelnest.fairies.share.dispatcher.model;

/**
 * Created by Inoria on 2019/3/24.
 */
public class FutureTaskRunningInfo {

    private Long startTimeMillis;
    private SliceDownloadTarget target;

    public Long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(Long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public SliceDownloadTarget getTarget() {
        return target;
    }

    public void setTarget(SliceDownloadTarget target) {
        this.target = target;
    }
}
