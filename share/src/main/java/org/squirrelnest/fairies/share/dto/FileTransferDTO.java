package org.squirrelnest.fairies.share.dto;

import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.dto.AbstractResult;

/**
 * 每个piece进行数据传输时，除了返回数据，还会返回节点当前的分片拥有情况，使下载者可以动态调度
 * Created by Inoria on 2019/3/22.
 */
public class FileTransferDTO {

    private HashCode160 fileId;

    private Integer sliceIndex;

    private Integer pieceIndex;

    private byte[] data;

    public FileTransferDTO() {

    }

    public FileTransferDTO(HashCode160 fileId, Integer sliceIndex, Integer pieceIndex) {
        this();
        this.fileId = fileId;
        this.sliceIndex = sliceIndex;
        this.pieceIndex = pieceIndex;
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

    public Integer getPieceIndex() {
        return pieceIndex;
    }

    public void setPieceIndex(Integer pieceIndex) {
        this.pieceIndex = pieceIndex;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public FileTransferDTO copyAnother(FileTransferDTO another) {
        setFileId(another.getFileId());
        setSliceIndex(another.getSliceIndex());
        setPieceIndex(another.getPieceIndex());
        setData(another.getData());
        return this;
    }
}
