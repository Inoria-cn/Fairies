package org.squirrelnest.fairies.file.cache.model;

import org.squirrelnest.fairies.common.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * Created by Inoria on 2019/3/25.
 */
public class SliceCacheContainer {
    private Integer sliceSize;
    private byte[][] data;

    public SliceCacheContainer(Integer sliceSize) {
        this.sliceSize = sliceSize;
        int pieceCount = sliceSize / PIECE_SIZE;
        data = new byte[pieceCount][];
        for (int i = 0; i < pieceCount; i++) {
            data[i] = null;
        }
    }

    private boolean isCacheFull() {
        for(byte[] piece : data) {
            if (piece == null) {
                return false;
            }
        }
        return true;
    }

    public boolean addPiece(int pieceIndex, byte[] pieceData) {
        data[pieceIndex] = pieceData;
        return isCacheFull();
    }

    public byte[] getPiece(int index) {
        return data[index];
    }

    public byte[] getWholeSlice() {
        byte[] result = new byte[sliceSize];
        int offsetCounter = 0;
        for (byte[] pieceData : data) {
            if (pieceData == null) {
                ArrayUtils.batchAppend(result, ArrayUtils.buildZeroByteArray(PIECE_SIZE),
                        PIECE_SIZE * offsetCounter++);
            }
            ArrayUtils.batchAppend(result, pieceData, PIECE_SIZE * offsetCounter++);
        }
        return result;
    }

    public List<Integer> getSlicePieceDataExistingIndexes() {
        List<Integer> result = new ArrayList<>(16);
        for(int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                result.add(i);
            }
        }
        return result;
    }

}