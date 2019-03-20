package org.squirrelnest.fairies.common.domain;

import org.squirrelnest.fairies.common.enumeration.FileState;
import org.squirrelnest.fairies.common.exception.SliceSizeInvalidException;
import org.squirrelnest.fairies.common.utils.MathUtils;
import org.squirrelnest.fairies.domain.HashCode160;

/**
 * 本地所拥有或拥有一部分的文件的信息记录
 * Created by Inoria on 2019/3/20.
 */
public class FileMetadata {

    public static final int MIN_SLICE_SIZE = 16 * 1024;

    /**
     * 文件整体的hashcode160
     */
    private HashCode160 id;
    /**
     * 文件名
     */
    private String name;
    /**
     * 本机绝对路径
     */
    private String path;
    /**
     * 文件状态
     */
    private FileState state;
    /**
     * 文件分片的拥有情况
     */
    private SliceBitmap slices;
    /**
     * 分片大小，取值必须是16KB * 2^n
     */
    private Integer sliceSize;
    /**
     * 最近修改时间
     */
    private Long updateTime;

    public HashCode160 getId() {
        return id;
    }

    public void setId(HashCode160 id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public FileState getState() {
        return state;
    }

    public void setState(FileState state) {
        this.state = state;
    }

    public SliceBitmap getSlices() {
        return slices;
    }

    public void setSlices(SliceBitmap slices) {
        this.slices = slices;
    }

    public Integer getSliceSize() {
        return sliceSize;
    }

    public void setSliceSize(Integer sliceSize) {
        if (!MathUtils.is2PowerN(sliceSize / MIN_SLICE_SIZE)) {
            throw new SliceSizeInvalidException();
        }
        this.sliceSize = sliceSize;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileMetadata)) return false;

        FileMetadata that = (FileMetadata) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
