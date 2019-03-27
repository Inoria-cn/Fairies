package org.squirrelnest.fairies.file.cache.model;

import org.squirrelnest.fairies.domain.HashCode160;

/**
 * Created by Inoria on 2019/3/25.
 */
public class FileAndSlice {
    private HashCode160 fileId;
    private Integer sliceIndex;

    public FileAndSlice(HashCode160 fileId, Integer sliceIndex) {
        this.fileId = fileId;
        this.sliceIndex = sliceIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileAndSlice)) return false;

        FileAndSlice that = (FileAndSlice) o;

        if (!fileId.equals(that.fileId)) return false;
        return sliceIndex.equals(that.sliceIndex);
    }

    @Override
    public int hashCode() {
        int result = fileId.hashCode();
        result = 31 * result + sliceIndex.hashCode();
        return result;
    }

    public HashCode160 getFileId() {
        return fileId;
    }

    public Integer getSliceIndex() {
        return sliceIndex;
    }
}
