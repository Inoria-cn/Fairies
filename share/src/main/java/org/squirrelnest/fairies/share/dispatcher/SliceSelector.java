package org.squirrelnest.fairies.share.dispatcher;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.squirrelnest.fairies.decorator.Decorator;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.local.domain.SliceDetail;
import org.squirrelnest.fairies.local.enumeration.SliceStateEnum;
import org.squirrelnest.fairies.share.dto.SliceBitmap;
import org.squirrelnest.fairies.share.dispatcher.model.SliceDownloadTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多线程下载任务分派与状态设置类，线程安全
 * Created by Inoria on 2019/3/22.
 */
public class SliceSelector {

    private final double SLICE_HOLD_RATE_LOW_LEVEL = 0.1;

    private final Map<Record, SliceBitmap> holderSlices;

    private final SliceDetail mySlices;

    /**
     * 记录文件提供方对应的最近一次下载分片的花费时间。
     */
    private final Map<Record, Long> holderLatestDownloadTime;

    public SliceSelector(Map<Record, SliceBitmap> holderSlices, SliceDetail mySlices) {
        this.holderSlices = holderSlices;
        this.mySlices = mySlices;
        this.holderLatestDownloadTime = new HashMap<>(16);
    }

    public Long getLastDownloadTimeAndRememberNewData(Record holder, Long newDownloadTime) {
        Long lastDownloadTime = holderLatestDownloadTime.getOrDefault(holder, -1L);
        holderLatestDownloadTime.put(holder, newDownloadTime);
        return lastDownloadTime;
    }

    public synchronized void beginDownloadSlice(int index) {
        mySlices.setSliceState(index, SliceStateEnum.DOWNLOADING);
    }

    public synchronized void downloadFailed(int index) {
        mySlices.setSliceState(index, SliceStateEnum.LACK_AND_FOUND);
    }

    public synchronized void downloadSuccess(int index) {
        mySlices.setSliceState(index, SliceStateEnum.HAVING);
    }

    public synchronized Boolean sliceExists(int index) {
        return mySlices.getSliceState(index).haveSlice();
    }

    public synchronized SliceDownloadTarget selectSliceIndex() {
        Double sliceHoldRate = mySlices.sliceHoldRate();
        if (sliceHoldRate < SLICE_HOLD_RATE_LOW_LEVEL) {
            return expectSpeedFastest();
        } else {
            return expectHoldRateLeast();
        }
    }

    public boolean downloadFinished() {
        return mySlices.hasAllFile();
    }

    public void updateSliceState(Record holder, SliceBitmap bitmap) {
        this.holderSlices.put(holder, bitmap);
    }


    /**
     * 刚开始下载时（拥有分片较少时），优先下载期望下载速度最快的分片
     */
    private SliceDownloadTarget expectSpeedFastest() {
        SliceDownloadTarget result = new SliceDownloadTarget();
        Record record = findDownloadSpeedFastRecord();
        if (record == null) {
            record = new ArrayList<>(holderLatestDownloadTime.keySet()).get(0);
        }
        Integer index = holderSlices.get(record).findFirstExpectSliceIndex(mySlices.generateBitmap());
        result.setTargetSliceHolder(record);
        result.setTargetSliceIndex(index);
        return result;
    }


    /**
     * 持有一定分片之后, 优先下载所有持有者节点中最少的分片
     */
    private SliceDownloadTarget expectHoldRateLeast() {
        int[] indexRankByHoldRate = calculateHoldRateIndexRank();
        for (int index : indexRankByHoldRate) {
            if (mySlices.getSliceState(index).haveSlice()) {
                continue;
            }
            Record targetHolder = findBestHolderForSlice(index);
            if (targetHolder == null) {
                continue;
            }

            return new SliceDownloadTarget(targetHolder, index);
        }
        return null;
    }

    private Record findBestHolderForSlice(int sliceIndex) {
        List<Record> candidates = new ArrayList<>(16);
        for (Map.Entry<Record, SliceBitmap> entry : holderSlices.entrySet()) {
            if (entry.getValue().valueAt(sliceIndex)) {
                candidates.add(entry.getKey());
            }
        }
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        }
        candidates.sort((c1, c2) -> {
            long time1 = holderLatestDownloadTime.getOrDefault(c1, -1L);
            long time2 = holderLatestDownloadTime.getOrDefault(c2, -1L);
            if (time1 <= 0 && time2 <= 0) {
                return 0;
            }
            if (time1 <= 0) {
                return 1;
            } else if(time2 <= 0) {
                return -1;
            }
            return (int)(time1 - time2);
        });
        return candidates.get(0);
    }

    private Record findDownloadSpeedFastRecord() {
        if (MapUtils.isEmpty(holderLatestDownloadTime)) {
            return null;
        }

        Record result = new ArrayList<>(holderLatestDownloadTime.keySet()).get(0);
        Long minTime = holderLatestDownloadTime.get(result);
        for (Map.Entry<Record, Long> entry : holderLatestDownloadTime.entrySet()) {
            if (minTime <= 0) {
                result = entry.getKey();
                minTime = entry.getValue();
                continue;
            }
            if (entry.getValue() > 0 && entry.getValue() < minTime) {
                result = entry.getKey();
                minTime = entry.getValue();
            }
        }
        if (minTime <= 0) {
            return null;
        }
        return result;
    }

    private int[] calculateHoldRateIndexRank() {
        int sliceSize = mySlices.getSliceSize();
        Double[] holdRates = new Double[sliceSize];
        SliceBitmap[] holderStates = new SliceBitmap[holderSlices.entrySet().size()];
        int index = 0;
        for (Map.Entry<Record, SliceBitmap> entry : holderSlices.entrySet()) {
            holderStates[index++] = entry.getValue();
        }
        for(int i = 0, holderSize = holderStates.length; i < sliceSize; i++) {
            int havingCount = 0;
            for (SliceBitmap bitmap : holderStates) {
                if (bitmap.valueAt(i)) {
                    havingCount++;
                }
            }
            holdRates[i] = havingCount * 1.0 / holderSize;
        }

        List<Decorator<Double>> sorted = new ArrayList<>(sliceSize);
        for (int i = 0; i < sliceSize; i++) {
            sorted.get(i).setData(holdRates[i]);
            sorted.get(i).put("index", i);
        }

        sorted.sort((decorator1, decorator2) -> decorator1.getData().compareTo(decorator2.getData()));
        int[] indexRank = new int[sliceSize];
        index = 0;
        for(Decorator<Double> decorator : sorted) {
            indexRank[index++] = (int)decorator.get("index");
        }

        return indexRank;
    }
}
