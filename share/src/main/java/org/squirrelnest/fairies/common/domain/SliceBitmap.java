package org.squirrelnest.fairies.common.domain;

import org.squirrelnest.fairies.common.exception.BitmapOutOfIndexException;
import org.squirrelnest.fairies.utils.BinaryUtils;

/**
 * Created by Inoria on 2019/3/20.
 */
public class SliceBitmap {

    private Byte[] bits;

    private Integer bitLength;

    public SliceBitmap(Byte[] bits, int length) {
        this.bits = bits;
        this.bitLength = length;
    }

    public Boolean valueAt(int index) {
        if (index > bitLength) {
            throw new BitmapOutOfIndexException();
        }
        int elementIndex = index / 8;
        int bitIndex = index % 8;
        return BinaryUtils.getBinaryDigit(bits[elementIndex], bitIndex) > 0;
    }

    public void makeValueAt(int index, boolean newValue) {
        if (index > bitLength) {
            throw new BitmapOutOfIndexException();
        }
        int elementIndex = index / 8;
        int bitIndex = index % 8;
        bits[elementIndex] = BinaryUtils.setDigit(bits[elementIndex], bitIndex, newValue);
    }


    /*
     * Getters and setters below is only for serialize.
     *
     */
    Byte[] getBits() {
        return bits;
    }

    void setBits(Byte[] bits) {
        this.bits = bits;
    }

    Integer getBitLength() {
        return bitLength;
    }

    void setBitLength(Integer bitLength) {
        this.bitLength = bitLength;
    }
}
