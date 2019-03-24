package org.squirrelnest.fairies.share.dto;

import org.squirrelnest.fairies.dto.AbstractResult;
import org.squirrelnest.fairies.local.enumeration.FileStateEnum;

/**
 * Created by Inoria on 2019/3/20.
 */
public class FileInfoDTO extends AbstractResult {

    private SliceBitmap sliceBitmap;

    private FileStateEnum fileState;

    public SliceBitmap getSliceBitmap() {
        return sliceBitmap;
    }

    public void setSliceBitmap(SliceBitmap sliceBitmap) {
        this.sliceBitmap = sliceBitmap;
    }

    public FileStateEnum getFileState() {
        return fileState;
    }

    public void setFileState(FileStateEnum fileState) {
        this.fileState = fileState;
    }
}
