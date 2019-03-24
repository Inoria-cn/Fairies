package org.squirrelnest.fairies.share.procedure;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.facade.DHTRequestFacade;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;
import org.squirrelnest.fairies.service.ConfigReadService;
import org.squirrelnest.fairies.service.TimeoutTaskService;
import org.squirrelnest.fairies.share.network.RequestService;

import javax.annotation.Resource;

/**
 * Created by Inoria on 2019/3/21.
 */
@Service
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

    public Download newDownloadInstance(HashCode160 fileId) {
        FileMetadata metadata = localFileInfoService.getFileData(fileId);
        if (metadata == null) {
            return null;
        }
        Download download = new Download(requestService, metadata, dhtRequestFacade, timeoutTaskService,
                configReadService.getDownloadThreadCount(), configReadService.getDownloadPieceTimeout(),
                configReadService.getDownloadMaxRetryTimes(), configReadService.getDownloadPieceTimeout());
        return download;
    }
}
