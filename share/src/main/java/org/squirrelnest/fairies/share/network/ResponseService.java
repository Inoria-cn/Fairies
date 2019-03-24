package org.squirrelnest.fairies.share.network;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.dto.AbstractResult;
import org.squirrelnest.fairies.file.RandomAccessService;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;
import org.squirrelnest.fairies.share.dto.FileInfoDTO;
import org.squirrelnest.fairies.share.dto.FileSliceHashDTO;
import org.squirrelnest.fairies.share.dto.FileTransferDTO;

import javax.annotation.Resource;

/**
 * Created by Inoria on 2019/3/20.
 */
@Service
public class ResponseService {

    @Resource(name = "localFileInfoService")
    private LocalFileInfoService fileDataContainer;

    @Resource(name = "randomAccessService")
    private RandomAccessService fileService;

    private <T extends AbstractResult> T buildFileNotFountResult(T origin) {
        origin.setReturnCode(AbstractResult.RETURN_CODE_VALUE_NOT_FOUNT);
        return origin;
    }

    public FileInfoDTO fileInfo(HashCode160 id) {
        FileInfoDTO result = new FileInfoDTO();
        if (!fileDataContainer.fileExist(id)) {
            return buildFileNotFountResult(result);
        }
        result.setSliceBitmap(fileDataContainer.getFileData(id).getSliceBitmap());
        result.setFileState(fileDataContainer.getFileData(id).getState());
        return result;
    }

    public FileTransferDTO downloadPiece(HashCode160 id, Integer sliceIndex, Integer pieceIndex) {
        FileMetadata metadata = fileDataContainer.getFileData(id);
        FileTransferDTO result = new FileTransferDTO(id, sliceIndex, pieceIndex);
        if (metadata == null) {
            return result;
        }
        byte[] data = fileService.getPieceData(metadata.getPath(), sliceIndex, pieceIndex, metadata.getSliceSize());
        result.setData(data);
        return result;
    }

    public FileSliceHashDTO sliceHash(HashCode160 id, Integer sliceIndex) {
        FileMetadata metadata = fileDataContainer.getFileData(id);
        FileSliceHashDTO result = new FileSliceHashDTO(id, sliceIndex);
        if (metadata == null) {
            return buildFileNotFountResult(result);
        }
        result.setSliceBitmap(metadata.getSliceBitmap());
        HashCode160 sliceHash = fileService.getSliceHash(metadata.getPath(), sliceIndex, metadata.getSliceSize());
        result.setSliceHash(sliceHash);
        return result;
    }
}
