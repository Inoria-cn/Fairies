package org.squirrelnest.fairies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.facade.DHTRequestFacade;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
import org.squirrelnest.fairies.kvpairs.keyword.model.File;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.share.service.FilePublishService;
import org.squirrelnest.fairies.share.service.FileDownloadService;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * fairies系统总入口点，对接视图层
 * Created by Inoria on 2019/3/24.
 */
@Service
public class FileHandleFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandleFacade.class);

    @Resource
    private DHTRequestFacade dhtFacade;

    @Resource
    private FilePublishService filePublishService;

    @Resource
    private FileDownloadService fileDownloadService;


    public List<File> searchFileByKeyword(String keyword) {
        return dhtFacade.getFilesByKeyword(keyword);
    }

    public int addFile2Local(HashCode160 fileId, String folderPath) {
        FileValue fileValue = dhtFacade.getFileInfoById(fileId, null);
        //can't find file info from DHT
        if (fileValue == null) {
            return -1;
        }
        FileMetadata localFileInfo = FileMetadata.transfer(fileValue, folderPath);
        boolean addResult = fileDownloadService.addLocalFileInfo(localFileInfo, fileValue.getSize());
        return addResult ? 0 : 1;
    }

    public List<FileMetadata> getCurrentLocalFileState() {
        return fileDownloadService.getCurrentLocalFileInfo();
    }

    public void downloadStart(HashCode160 fileId) {
        fileDownloadService.startDownload(fileId);
    }

    public void downloadPause(HashCode160 fileId) {
        fileDownloadService.pauseDownload(fileId);
    }

    public void downloadStop(HashCode160 fileId) {
        fileDownloadService.stopDownload(fileId);
    }

    public void deleteFromLocalFileLibrary(HashCode160 fileId) {
        fileDownloadService.deleteLocalFileInfo(fileId);
    }

    public Boolean publishShareFile(java.io.File file, String author, List<String> keywords) {
        try {
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
            filePublishService.filePublish(file.getAbsolutePath(), file.getName(), author, keywords);
            return true;
        } catch (Exception e) {
            LOGGER.error("File publishing raised an error.", e);
            return false;
        }
    }
}
