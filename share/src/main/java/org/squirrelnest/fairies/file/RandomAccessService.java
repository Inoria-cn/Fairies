package org.squirrelnest.fairies.file;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.file.cache.PieceWriteCache;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.local.enumeration.SliceStateEnum;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;

import javax.annotation.Resource;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * access with local file system. Cache is in need.
 * Created by Inoria on 2019/3/22.
 */
@Service
public class RandomAccessService {

    @Resource(name = "pieceWriteCache")
    private PieceWriteCache writeCache;

    @Resource
    private FileIOCoreService fileIOCoreService;

    @Resource(name = "localFileInfoService")
    private LocalFileInfoService fileInfoService;

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAccessService.class);

    public byte[] getPieceData(HashCode160 fileId, String absolutePath, Integer sliceIndex, Integer pieceIndex, Integer sliceSize) {
        if (fileInfoService.getFileData(fileId).getSlices().getSliceState(sliceIndex).equals(SliceStateEnum.PARTLY)) {
            byte[] pieceData = writeCache.readPieceInCache(fileId, sliceIndex, pieceIndex);
            if (pieceData != null) {
                return pieceData;
            }
        }
        return fileIOCoreService.getPieceData(absolutePath, sliceIndex, pieceIndex, sliceSize);
    }


    public void savePiece(HashCode160 fileId, Integer sliceIndex, Integer pieceIndex, byte[] data) {
        writeCache.writePiece(fileId, sliceIndex, pieceIndex, data);
    }

    public HashCode160 calculateSliceHash(HashCode160 fileId, int sliceIndex) {
        FileMetadata fileMetadata = fileInfoService.getFileData(fileId);
        if (!fileMetadata.getSlices().getSliceState(sliceIndex).haveSlice()) {
            return null;
        }
        MessageDigest sha1Digest = DigestUtils.getSha1Digest();
        sha1Digest.update(fileIOCoreService.getSliceData(fileMetadata.getPath(), sliceIndex, fileMetadata.getSliceSize()));
        return new HashCode160(sha1Digest.digest());
    }

    public void invalidSlice(HashCode160 fileId, int sliceIndex) {
        fileInfoService.getFileData(fileId).getSlices().setSliceState(sliceIndex, SliceStateEnum.LACK_AND_FOUND);
    }


}
