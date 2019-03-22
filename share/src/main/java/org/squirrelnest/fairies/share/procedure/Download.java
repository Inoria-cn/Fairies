package org.squirrelnest.fairies.share.procedure;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.squirrelnest.fairies.decorator.Decorator;
import org.squirrelnest.fairies.share.dto.FileInfoResult;
import org.squirrelnest.fairies.share.enumeration.DownloadStateEnum;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.facade.DHTRequestFacade;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
import org.squirrelnest.fairies.share.service.RequestService;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;
import org.squirrelnest.fairies.thread.CallableWithInform;
import org.squirrelnest.fairies.thread.inform.ResponseCountInform;
import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 文件下载逻辑：建立文件本地信息对象，通过DHT找到文件kv信息，从中获取持有文件全部或者一部分的其他节点。
 * 依次询问持有该文件的节点的分片持有情况, 更新本地文件信息对象
 * 发送DHT数据更新请求，文件持有者加上本节点，同时减去没有回复或者并未持有该文件的目标节点。
 * 多线程请求不同分片并且进行下载，如果目标节点的文件持有情况发生了改变，主动通知本节点。
 * Created by Inoria on 2019/3/21.
 */
public class Download {

    private final static String DECORATOR_KEY_REQUEST_TIME = "request";

    private final LocalFileInfoService fileInfoService;

    private final RequestService requestService;

    private final DHTRequestFacade dhtFacade;

    private final ExecutorService threadPool;

    private final long requestTimeout;

    private HashCode160 fileId;

    private Map<Record, FileInfoResult> nodeFileData;

    private FileValue fileDHTData;

    private DownloadStateEnum downloadState = DownloadStateEnum.NEW;

    private List<Record> fileDHTDataHolderContainer = new ArrayList<>(16);

    Download(RequestService requestService, LocalFileInfoService fileInfoService, DHTRequestFacade dhtRequestFacade,
             int multiThreadDegree, HashCode160 fileId, long requestTimeoutMillis) {
        this.requestService = requestService;
        this.fileInfoService = fileInfoService;
        this.dhtFacade = dhtRequestFacade;
        this.threadPool = new ThreadPoolExecutor(multiThreadDegree, 4 * multiThreadDegree,
                3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        this.fileId = fileId;
        this.requestTimeout = requestTimeoutMillis;
    }

    /**
     * 下载过程是多线程的，在暂停或者全部下载完成前，该方法会阻塞，因此调用处应该放在独立的线程中。
     */
    public void start() {
        if (!fetchFileHolderData()) {
            return;
        }
        if (!connectFileHolders()) {
            return;
        }
    }

    private boolean fetchFileHolderData() {
        this.downloadState = DownloadStateEnum.FIND_FILE_OWNER;
        this.fileDHTData = dhtFacade.getFileInfoById(this.fileId, this.fileDHTDataHolderContainer);
        if (this.fileDHTData == null || CollectionUtils.isEmpty(this.fileDHTData.getHolders())) {
            this.downloadState = DownloadStateEnum.FILE_OWNER_NOT_FOUND;
            return false;
        }
        return true;
    }

    private boolean connectFileHolders() {
        this.downloadState = DownloadStateEnum.CONNECT_WITH_FILE_OWNER;
        List<Record> fileHolders = this.fileDHTData.getHolders();
        //用于标识该节点是由于线程池限制没有被及时请求，还是已经请求但是超时了
        List<Decorator<Record>> requestedFileHolders = new ArrayList<>(fileHolders.size());
        Inform<Integer> informer = new ResponseCountInform();
        for (Record server : fileHolders) {
            CallableWithInform<FileInfoResult, Integer> task = new CallableWithInform<FileInfoResult, Integer>() {
                @Override
                public FileInfoResult originCall() throws Exception {
                    Decorator<Record> serverDecorator = new Decorator<>(server);
                    serverDecorator.put(DECORATOR_KEY_REQUEST_TIME, System.currentTimeMillis());
                    requestedFileHolders.add(serverDecorator);

                    FileInfoResult fileInfo = requestService.requestFileInfo(server, fileId);
                    nodeFileData.put(server, fileInfo);
                    return fileInfo;
                }
            };
            task.setInform(informer);
            threadPool.submit(task);
        }
        informer.blockUntilState(fileHolders.size(), requestTimeout);
        long nowTimestamp = System.currentTimeMillis();
        threadPool.shutdownNow();

        List<Record> newValidHolders = getNewValidFileHolders(requestedFileHolders, nowTimestamp);
        dhtFacade.asyncUpdateFileHolders(this.fileDHTDataHolderContainer, this.fileDHTData, newValidHolders);

        if (MapUtils.isNotEmpty(this.nodeFileData)) {
            return true;
        } else {
            this.downloadState = DownloadStateEnum.FILE_OWNER_NOT_FOUND;
            return false;
        }
    }

    /**
     * 获取新的有效文件提供者列表，用于更新dht
     */
    private List<Record> getNewValidFileHolders(List<Decorator<Record>> requestedFileHolders, long now) {
        List<Record> validFileHolders = new ArrayList<>(this.fileDHTData.getHolders().size());
        for (Record holder : this.fileDHTData.getHolders()) {
            if (this.nodeFileData.containsKey(holder) && this.nodeFileData.get(holder).getFileState().hasFile()) {
                validFileHolders.add(holder);
                continue;
            }
            Decorator<Record> decorated = Decorator.findDecorated(requestedFileHolders, holder);
            if (decorated != null &&
                    now - (long)decorated.getOrDefault(DECORATOR_KEY_REQUEST_TIME, 0) <= this.requestTimeout) {
                validFileHolders.add(holder);
            }
        }
        validFileHolders.add(dhtFacade.getLocalRecord());
        return validFileHolders;
    }
}
