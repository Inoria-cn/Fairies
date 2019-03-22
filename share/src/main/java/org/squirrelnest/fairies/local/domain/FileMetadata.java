package org.squirrelnest.fairies.local.domain;

import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.local.enumeration.FileStateEnum;
import org.squirrelnest.fairies.share.dto.SliceBitmap;
import org.squirrelnest.fairies.common.exception.SliceSizeInvalidException;
import org.squirrelnest.fairies.common.utils.MathUtils;
import org.squirrelnest.fairies.domain.HashCode160;

import java.util.Map;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * 本地所拥有或拥有一部分的文件的信息记录
 * Created by Inoria on 2019/3/20.
 */
public class FileMetadata {

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
    private FileStateEnum state;
    /**
     * 文件分片的拥有情况
     */
    private SliceDetail slices;
    /**
     * 分片大小，取值必须是16KB * 2^n
     */
    private Integer sliceSize;
    /**
     * 最近修改时间
     */
    private Long updateTime;
    /**
     * 文件持有者和对应的分片持有状态
     */
    private Map<Record, SliceBitmap> holders;

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

    public FileStateEnum getState() {
        return state;
    }

    public void setState(FileStateEnum state) {
        this.state = state;
    }

    public SliceDetail getSlices() {
        return slices;
    }

    public void setSlices(SliceDetail slices) {
        this.slices = slices;
    }

    public Integer getSliceSize() {
        return sliceSize;
    }

    public void setSliceSize(Integer sliceSize) {
        if (!MathUtils.is2PowerN(sliceSize / PIECE_SIZE)) {
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

    public Map<Record, SliceBitmap> getHolders() {
        return holders;
    }

    public void setHolders(Map<Record, SliceBitmap> holders) {
        this.holders = holders;
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
