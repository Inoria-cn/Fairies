package org.squirrelnest.fairies.share.dto;

import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.dto.AbstractResult;

/**
 * Created by Inoria on 2019/3/24.
 */
public class FileSliceHashDTO extends AbstractResult {

    private SliceBitmap sliceBitmap;

    private HashCode160 fileId;

    private Integer sliceIndex;

    private HashCode160 sliceHash;

    public FileSliceHashDTO() {

    }

    public FileSliceHashDTO(HashCode160 fileId, Integer sliceIndex) {
        this();
        this.fileId = fileId;
        this.sliceIndex = sliceIndex;
    }

    public SliceBitmap getSliceBitmap() {
        return sliceBitmap;
    }

    public void setSliceBitmap(SliceBitmap sliceBitmap) {
        this.sliceBitmap = sliceBitmap;
    }

    public HashCode160 getFileId() {
        return fileId;
    }

    public void setFileId(HashCode160 fileId) {
        this.fileId = fileId;
    }

    public Integer getSliceIndex() {
        return sliceIndex;
    }

    public void setSliceIndex(Integer sliceIndex) {
        this.sliceIndex = sliceIndex;
    }

    public HashCode160 getSliceHash() {
        return sliceHash;
    }

    public void setSliceHash(HashCode160 sliceHash) {
        this.sliceHash = sliceHash;
    }
}
