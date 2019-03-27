package org.squirrelnest.fairies.local.domain;

import org.squirrelnest.fairies.local.enumeration.SliceStateEnum;
import org.squirrelnest.fairies.share.dto.SliceBitmap;
import org.squirrelnest.fairies.utils.BinaryUtils;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * Created by Inoria on 2019/3/22.
 */
public class SliceDetail {

    private SliceStateEnum[] sliceStates;

    private SliceBitmap[] pieceBitmaps;

    private final Integer sliceCount;

    private final Integer sliceSize;

    public SliceDetail(Integer sliceCount, Integer sliceSize) {
        this.sliceCount = sliceCount;
        this.sliceSize = sliceSize;
        pieceBitmaps = new SliceBitmap[sliceCount];
        for (int i = 0; i < pieceBitmaps.length; i++) {
            pieceBitmaps[i] = null;
        }
    }

    public SliceDetail(SliceStateEnum[] sliceStates, Integer sliceCount, Integer sliceSize) {
        this(sliceCount, sliceSize);
        this.sliceStates = sliceStates;
    }

    public SliceStateEnum[] getSliceStates() {
        return sliceStates;
    }

    public void setSliceStates(SliceStateEnum[] sliceStates) {
        this.sliceStates = sliceStates;
    }

    public SliceStateEnum getSliceState(int index) {
        return this.sliceStates[index];
    }

    public synchronized void setSliceState(int index, SliceStateEnum newState) {
        this.sliceStates[index] = newState;
    }

    public Integer getSliceCount() {
        return sliceCount;
    }

    public Integer getSliceSize() {
        return sliceSize;
    }


    /**
     * @return 是否拥有该文件的数据或者一部分数据
     */
    public boolean hasPartFile() {
        for(SliceStateEnum state : sliceStates) {
            if(state.haveSlice()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllFile() {
        for (SliceStateEnum state : sliceStates) {
            if (!state.haveSlice()) {
                return false;
            }
        }
        return true;
    }

    public SliceBitmap generateBitmap() {
        boolean[] havingStatus = new boolean[sliceStates.length];
        for(int i = 0; i < sliceStates.length; i++) {
            havingStatus[i] = sliceStates[i].haveSlice();
        }

        return new SliceBitmap(BinaryUtils.getByteArrayFromBooleanArray(havingStatus), sliceStates.length);
    }

    public double sliceHoldRate() {
        int sliceExistCount = 0;
        for (SliceStateEnum sliceStateCode : sliceStates) {
            sliceExistCount += sliceStateCode.haveSlice() ? 1 : 0;
        }
        return 1.0 * sliceExistCount / sliceCount;
    }

    public boolean sliceHasAllPiece(int sliceIndex) {
        SliceStateEnum state = sliceStates[sliceIndex];
        if (SliceStateEnum.HAVING.equals(state)) {
            return true;
        }
        if (SliceStateEnum.PARTLY.equals(state)) {
            if (pieceBitmaps[sliceIndex] == null) {
                return false;
            }
            if (pieceBitmaps[sliceIndex].hasAllParts()) {
                sliceStates[sliceIndex] = SliceStateEnum.HAVING;
                return true;
            }
        }
        return false;
    }

    public synchronized void sliceSavePiece(int sliceIndex, int pieceIndex) {
        SliceBitmap pieceBitmap = pieceBitmaps[sliceIndex];
        if (pieceBitmap == null) {
            pieceBitmaps[sliceIndex] = new SliceBitmap(sliceSize / PIECE_SIZE);
            pieceBitmap = pieceBitmaps[sliceIndex];
        }
        pieceBitmap.makeValueAt(pieceIndex, true);
        sliceStates[sliceIndex] = SliceStateEnum.PARTLY;
    }
}
