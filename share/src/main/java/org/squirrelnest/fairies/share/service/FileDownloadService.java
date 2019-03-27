package org.squirrelnest.fairies.share.service;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.file.RandomAccessService;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;
import org.squirrelnest.fairies.service.ConfigReadService;
import org.squirrelnest.fairies.share.enumeration.DownloadStateEnum;
import org.squirrelnest.fairies.share.procedure.Download;
import org.squirrelnest.fairies.share.procedure.ProcedureFactory;

import javax.annotation.Resource;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责下载任务的记录，下载状态的管理, 文件分享模块的入口类
 * Created by Inoria on 2019/3/27.
 */
@Service
public class FileDownloadService {

    @Resource
    private ConfigReadService configReadService;

    @Resource
    private LocalFileInfoService fileInfoService;

    @Resource
    private RandomAccessService ioService;

    //与本地记录中的文件相关联的下载过程，停止下载时从中删除。
    private Map<FileMetadata, Download> downloading = new HashMap<>(16);

    public List<FileMetadata> getCurrentLocalFileInfo() {
        return fileInfoService.getCurrentFileInfo();
    }

    public boolean addLocalFileInfo(FileMetadata fileMetadata, Integer fileSize) {
        HashCode160 id = fileMetadata.getId();
        if (!fileInfoService.fileExist(id)) {
            return false;
        }
        fileInfoService.putFileData(fileMetadata.getId(), fileMetadata);
        File file = new File(fileMetadata.getPath());

        return ioService.createFixedSizeFileIfNotExist(file, fileSize);
    }

    public boolean deleteLocalFileInfo(HashCode160 id) {
        return fileInfoService.deleteFileInfo(id);
    }

    public boolean stopDownload(HashCode160 id) {
        for (Map.Entry<FileMetadata, Download> entry : downloading.entrySet()) {
            if (entry.getKey().getId().equals(id)) {
                entry.getValue().stop();
                downloading.remove(entry.getKey());
                return true;
            }
        }
        return false;
    }

    public boolean pauseDownload(HashCode160 id) {
        Download downloading = findDownloadProcedureById(id);
        if (downloading == null) {
            return false;
        }
        downloading.stop();
        return true;
    }

    public boolean startDownload(HashCode160 id) {
        //如果文件不在本地元数据中，无法开始下载，返回false
        FileMetadata localFileInfo = fileInfoService.getFileData(id);
        if (localFileInfo == null) {
            return false;
        }
        //如果没有创建该文件对应的下载任务，创建并且放入map中
        Download downloadTask = findDownloadProcedureById(id);
        if (downloadTask == null) {
            downloadTask = ProcedureFactory.procedureDownload(id);
            downloading.put(localFileInfo, downloadTask);
        }
        //如果当前下载的文件数已经超过最大文件数，不能开始下载
        int downloadingCounter = 0;
        for (Map.Entry<FileMetadata, Download> entry : downloading.entrySet()) {
            if (entry.getValue().getDownloadState().equals(DownloadStateEnum.DOWNLOADING)) {
                downloadingCounter++;
            } else if (!entry.getValue().notFinished()) {
                downloading.remove(entry.getKey());
            }
        }
        if (downloadingCounter >= configReadService.getDownloadMultiTaskCount()) {
            return false;
        }
        //开始下载并返回true
        downloadTask.start();
        return true;
    }



    private Download findDownloadProcedureById(HashCode160 fileId) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(fileId);
        return downloading.get(fileMetadata);
    }

}
