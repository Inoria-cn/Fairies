package org.squirrelnest.fairies.share.procedure;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.facade.DHTRequestFacade;
import org.squirrelnest.fairies.file.RandomAccessService;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;
import org.squirrelnest.fairies.service.ConfigReadService;
import org.squirrelnest.fairies.service.TimeoutTaskService;
import org.squirrelnest.fairies.share.network.RequestService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * Created by Inoria on 2019/3/21.
 */
@Component
public class ProcedureFactory {

    @Resource
    private ConfigReadService configReadService;

    @Resource
    private RequestService requestService;

    @Resource
    private LocalFileInfoService localFileInfoService;

    @Resource
    private DHTRequestFacade dhtRequestFacade;

    @Resource
    private TimeoutTaskService timeoutTaskService;

    @Resource
    private RandomAccessService randomAccessService;


    private static ProcedureFactory self;

    @PostConstruct
    private void init() {
        self = this;
    }

    private ProcedureFactory() {

    }

    public static Download procedureDownload(HashCode160 fileId) {
        return self.newDownloadInstance(fileId);
    }

    private Download newDownloadInstance(HashCode160 fileId) {
        FileMetadata metadata = localFileInfoService.getFileData(fileId);
        if (metadata == null) {
            return null;
        }
        return new Download(requestService, metadata, randomAccessService, dhtRequestFacade, timeoutTaskService,
                configReadService.getDownloadThreadCount(), configReadService.getDownloadPieceTimeout(),
                configReadService.getDownloadMaxRetryTimes(), configReadService.getDownloadPieceTimeout());
    }
}
