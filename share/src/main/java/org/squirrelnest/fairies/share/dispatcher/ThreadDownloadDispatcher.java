package org.squirrelnest.fairies.share.dispatcher;

import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.service.TimeoutTaskService;
import org.squirrelnest.fairies.share.dispatcher.model.SliceDownloadTarget;
import org.squirrelnest.fairies.share.dto.FileSliceHashDTO;
import org.squirrelnest.fairies.share.dto.FileTransferDTO;
import org.squirrelnest.fairies.share.network.RequestService;
import org.squirrelnest.fairies.thread.CallableWithInform;
import org.squirrelnest.fairies.thread.inform.OneResponseInform;
import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

import java.util.concurrent.*;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * 多线程下载核心类
 * 对应一个文件的下载，建立一个固定大小的线程池，每个线程负责一个slice的下载
 * Created by Inoria on 2019/3/22.
 */
public class ThreadDownloadDispatcher {

    private final SliceSelector sliceSelector;

    /**
     * 该线程池的两个要求：1.每当有一个任务执行完成，可以得到通知； 2.可以设定任务超时时间，超时任务停止执行释放线程资源。
     */
    private final ExecutorService threadPool;

    private final RequestService requestService;

    private final TimeoutTaskService timeoutTaskService;

    private final HashCode160 fileId;

    private final Integer sliceSize;

    private final Integer maxPieceFail;
    
    private final Long pieceDownloadTimeoutMillis;

    private final int totalThreadCount;

    private int runningThreadCount = 0;

    private boolean running = true;

    public ThreadDownloadDispatcher(FileMetadata fileMetadata, RequestService requestService, TimeoutTaskService timeoutTaskService,
                                    int multiThreadCount, int maxPieceFail, long pieceDownloadTimeoutMillis) {
        this.requestService = requestService;
        this.timeoutTaskService = timeoutTaskService;

        this.fileId = fileMetadata.getId();
        this.sliceSize = fileMetadata.getSliceSize();
        this.totalThreadCount = multiThreadCount;
        this.maxPieceFail = maxPieceFail;
        this.pieceDownloadTimeoutMillis = pieceDownloadTimeoutMillis;

        this.sliceSelector = new SliceSelector(fileMetadata.getHolders(), fileMetadata.getSlices());
        this.threadPool = new ThreadPoolExecutor(multiThreadCount, multiThreadCount,
                0, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * 该方法会阻塞，应当放在独立线程中运行。
     */
    public void run() {
        running = true;

        Inform<Boolean> informer = new OneResponseInform();
        while(running) {
            if (sliceSelector.downloadFinished()) {
                break;
            }
            if (runningThreadCount < totalThreadCount) {
                dispatchTask(informer);
            } else {
                blockUntilThreadAvailable(informer);
            }
        }
    }

    public void stop() {
        running = false;
    }

    private synchronized void countRunningThread(boolean isStart) {
        if (isStart) {
            this.runningThreadCount++;
        } else {
            runningThreadCount--;
        }
    }

    /**
     * 选择slice后，每个线程依次下载其中的piece，下载完成后验证slice hash，并更新sliceBitmap
     * 每个piece具有超时时间，超时后中断请求并重试，超过最大重试次数则标记为失败并且终止该slice的下载，线程重置。
     */
    private void dispatchTask(Inform<Boolean> informer) {
        CallableWithInform<Boolean, Boolean> task = new CallableWithInform<Boolean, Boolean>() {
            @Override
            public Boolean originCall() throws Exception {
                countRunningThread(true);

                SliceDownloadTarget sliceInfo = sliceSelector.selectSliceIndex();
                final Record holder = sliceInfo.getTargetSliceHolder();
                final int sliceIndex = sliceInfo.getTargetSliceIndex();
                sliceSelector.beginDownloadSlice(sliceIndex);

                int pieceDownloadTimeoutCount = 0;
                boolean interrupted = false;
                for(int i = 0, pieceCount = sliceSize / PIECE_SIZE; i < pieceCount; i++) {
                    if (pieceDownloadTimeoutCount > maxPieceFail) {
                        interrupted = true;
                        break;
                    }

                    final int pieceIndex = i;
                    TimeoutTaskService.TaskRunState state = timeoutTaskService.new TaskRunState();
                    FileTransferDTO pieceData = timeoutTaskService.runTaskInTime(
                            () -> requestService.requestPieceData(holder, fileId, sliceIndex, pieceIndex),
                            pieceDownloadTimeoutMillis, state);
                    
                    if(state.getTaskRunSuccess() && pieceData != null) {
                        pieceDownloadTimeoutCount = 0;
                        sliceSelector.getLastDownloadTimeAndRememberNewData(holder, state.getTaskRunMillis());
                        saveService.savePiece(fileId, sliceIndex, pieceIndex, pieceData.getData());
                    } else {
                        pieceDownloadTimeoutCount++;
                        i--;
                        sliceSelector.getLastDownloadTimeAndRememberNewData(holder, -1L);
                    }
                }

                if(interrupted) {
                    sliceSelector.downloadFailed(sliceIndex);
                    return false;
                } else {
                    HashCode160 localHash = saveService.calculateSliceHash(fileId, sliceIndex);
                    FileSliceHashDTO remoteHash = timeoutTaskService.runTaskInTime(
                            () -> requestService.requestSliceHash(
                                    holder, fileId, sliceIndex), 
                            pieceDownloadTimeoutMillis, null
                    );
                    if (remoteHash != null) {
                        sliceSelector.updateSliceState(holder, remoteHash.getSliceBitmap());
                    }
                    if (remoteHash != null && localHash.equals(remoteHash.getSliceHash())) {
                        sliceSelector.downloadSuccess(sliceIndex);
                        return true;
                    } else {
                        sliceSelector.downloadFailed(sliceIndex);
                        saveService.invalidSlice(fileId, sliceIndex);
                        return false;
                    }
                }
            }
        };
        task.setInform(informer);
        task.setAppendTask(() -> countRunningThread(false));
        threadPool.submit(task);
    }

    private void blockUntilThreadAvailable(Inform<Boolean> informer) {
        informer.setState(false);
        informer.blockUntilState(true, 3000L);
    }
}
