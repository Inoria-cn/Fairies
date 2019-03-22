package org.squirrelnest.fairies.share.service;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.dto.AbstractResult;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;
import org.squirrelnest.fairies.share.dto.FileInfoResult;

import javax.annotation.Resource;

/**
 * Created by Inoria on 2019/3/20.
 */
@Service
public class ResponseService {

    @Resource(name = "localFileInfoService")
    private LocalFileInfoService fileDataContainer;

    public FileInfoResult fileInfo(HashCode160 id) {
        FileInfoResult result = new FileInfoResult();
        if (!fileDataContainer.fileExist(id)) {
            result.setReturnCode(AbstractResult.RETURN_CODE_VALUE_NOT_FOUNT);
            return result;
        }
        result.setFileMetadata(fileDataContainer.getFileData(id));
        return result;
    }
}
