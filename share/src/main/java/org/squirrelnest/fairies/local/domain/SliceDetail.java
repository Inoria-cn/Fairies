package org.squirrelnest.fairies.local.domain;

import org.squirrelnest.fairies.local.enumeration.SliceStateCodeEnum;
import org.squirrelnest.fairies.share.dto.SliceBitmap;
import org.squirrelnest.fairies.utils.BinaryUtils;

/**
 * Created by Inoria on 2019/3/22.
 */
public class SliceDetail {

    private SliceStateCodeEnum[] sliceStates;

    private Integer sliceCount;

    private Integer sliceSize;

    public SliceStateCodeEnum[] getSliceStates() {
        return sliceStates;
    }

    public void setSliceStates(SliceStateCodeEnum[] sliceStates) {
        this.sliceStates = sliceStates;
    }

    public Integer getSliceCount() {
        return sliceCount;
    }

    public void setSliceCount(Integer sliceCount) {
        this.sliceCount = sliceCount;
    }

    public Integer getSliceSize() {
        return sliceSize;
    }

    public void setSliceSize(Integer sliceSize) {
        this.sliceSize = sliceSize;
    }

    /**
     * @return 是否拥有该文件的数据或者一部分数据
     */
    public boolean hasFile() {
        for(SliceStateCodeEnum state : sliceStates) {
            if(SliceStateCodeEnum.HAVING.equals(state)) {
                return true;
            }
        }
        return false;
    }

    public SliceBitmap generateBitmap() {
        boolean[] havingStatus = new boolean[sliceStates.length];
        for(int i = 0; i < sliceStates.length; i++) {
            havingStatus[i] = sliceStates[i].haveSlice();
        }

        return new SliceBitmap(BinaryUtils.getByteArrayFromBooleanArray(havingStatus), sliceStates.length);
    }
}
