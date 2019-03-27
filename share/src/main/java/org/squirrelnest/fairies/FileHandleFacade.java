package org.squirrelnest.fairies;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.facade.DHTRequestFacade;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
import org.squirrelnest.fairies.kvpairs.keyword.model.File;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.share.service.FilePublishService;
import org.squirrelnest.fairies.share.service.FileDownloadService;

import javax.annotation.Resource;
import java.util.List;

/**
 * fairies系统总入口点，对接视图层
 * Created by Inoria on 2019/3/24.
 */
@Service
public class FileHandleFacade {

    @Resource
    private DHTRequestFacade dhtFacade;

    @Resource
    private FilePublishService filePublishService;

    @Resource
    private FileDownloadService fileDownloadService;


    public List<File> searchFileByKeyword(String keyword) {
        return dhtFacade.getFilesByKeyword(keyword);
    }

    public Boolean addDownloadTask(HashCode160 fileId, String folderPath) {
        FileValue fileValue = dhtFacade.getFileInfoById(fileId, null);
        if (fileValue == null) {
            return false;
        }

    }

    public List<FileMetadata> getCurrentDownloadState() {

    }

    public void downloadStart(HashCode160 fileId) {

    }

    public void downloadStop(HashCode160 fileId) {

    }

    public void deleteFromLocalFileLibrary(HashCode160 fileId) {

    }

    public Boolean publishShareFile(FileMetadata fileMetadata) {

    }
}
