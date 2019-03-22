package org.squirrelnest.fairies.share.dto;

import org.squirrelnest.fairies.domain.HashCode160;

/**
 * 每个piece进行数据传输时，除了返回数据，还会返回节点当前的分片拥有情况，使下载者可以动态调度
 * Created by Inoria on 2019/3/22.
 */
public class FileTransferDTO {

    private SliceBitmap sliceBitmap;

    private HashCode160 fileId;

    private Integer sliceIndex;

    private Integer pieceIndex;

    private byte[] data;
}
