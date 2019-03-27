package org.squirrelnest.fairies.share.dto;

import org.squirrelnest.fairies.common.exception.BitmapOutOfIndexException;
import org.squirrelnest.fairies.utils.BinaryUtils;

/**
 * Used in network request for status of having slices.
 * Created by Inoria on 2019/3/20.
 */
public class SliceBitmap {

    private byte[] bits;

    private Integer bitLength;

    public SliceBitmap(byte[] bits, int length) {
        this.bits = bits;
        this.bitLength = length;
    }

    public SliceBitmap(int length) {
        this.bits = new byte[(length - 1) / 4 + 1];
        this.bitLength = length;
        for (int i = 0 ; i < bits.length; i++) {
            bits[i] = 0;
        }
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

    public Integer findFirstExpectSliceIndex(SliceBitmap having) {
        for (int i = 0; i < bitLength; i++) {
            if (this.valueAt(i) && !having.valueAt(i)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasAllParts() {
        for (int i = 0; i < bitLength; i++) {
            if (!valueAt(i)) {
                return false;
            }
        }
        return true;
    }

    /*
     * Getters and setters below is only for serialize.
     *
     */
    byte[] getBits() {
        return bits;
    }

    void setBits(byte[] bits) {
        this.bits = bits;
    }

    Integer getBitLength() {
        return bitLength;
    }

    void setBitLength(Integer bitLength) {
        this.bitLength = bitLength;
    }
}
