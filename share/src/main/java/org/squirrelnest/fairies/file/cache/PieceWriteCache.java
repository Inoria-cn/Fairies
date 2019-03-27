package org.squirrelnest.fairies.file.cache;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.file.FileIOCoreService;
import org.squirrelnest.fairies.file.cache.model.FileAndSlice;
import org.squirrelnest.fairies.file.cache.model.SliceCacheContainer;
import org.squirrelnest.fairies.local.domain.SliceDetail;
import org.squirrelnest.fairies.local.enumeration.SliceStateEnum;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;
import org.squirrelnest.fairies.service.ConfigReadService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * Created by Inoria on 2019/3/24.
 */
@Service
public class PieceWriteCache {

    @Resource(name = "fileIOCoreService")
    private FileIOCoreService saveService;

    @Resource(name = "localFileInfoService")
    private LocalFileInfoService fileInfoService;

    @Resource
    private ConfigReadService configReadService;

    private Integer maxMemoryCost;

    private int currentMemoryCost = 0;

    private Map<FileAndSlice, SliceCacheContainer> cache;

    @PostConstruct
    private void init() {
        cache = new HashMap<>(16);
        maxMemoryCost = configReadService.getOutputMaxCacheSize();
    }

    public synchronized void writeAllCache2Storage() {

        for (Map.Entry<FileAndSlice, SliceCacheContainer> entry : cache.entrySet()) {
            SliceDetail sliceInfo = fileInfoService.getFileData(entry.getKey().getFileId()).getSlices();
            String filePath = fileInfoService.getFileData(entry.getKey().getFileId()).getPath();
            Integer sliceSize = fileInfoService.getFileData(entry.getKey().getFileId()).getSliceSize();
            int sliceIndex = entry.getKey().getSliceIndex();
            byte[] sliceData = entry.getValue().getWholeSlice();
            List<Integer> pieceExistingIndexes = entry.getValue().getSlicePieceDataExistingIndexes();
            saveService.writeSliceData(filePath, sliceIndex, sliceSize, sliceData);
            for (Integer index : pieceExistingIndexes) {
                sliceInfo.sliceSavePiece(sliceIndex, index);
            }
            sliceInfo.sliceHasAllPiece(sliceIndex);
        }
        cache.clear();
        currentMemoryCost = 0;
    }

    public byte[] readPieceInCache(HashCode160 fileId, int sliceIndex, int pieceIndex) {
        FileAndSlice fileAndSlice = new FileAndSlice(fileId, sliceIndex);
        SliceCacheContainer container = cache.get(fileAndSlice);
        if (container == null) {
            return null;
        }

        return container.getPiece(pieceIndex);
    }

    public boolean writePiece(HashCode160 fileId, int sliceIndex, int pieceIndex, byte[] data) {
        String fileAbsolutePath = fileInfoService.getFileData(fileId).getPath();
        SliceDetail sliceInfo = fileInfoService.getFileData(fileId).getSlices();
        int sliceSize = sliceInfo.getSliceSize();
        if (shouldWriteDirectly(fileId, sliceIndex, sliceSize)) {
            saveService.writePieceData(fileAbsolutePath, sliceIndex, pieceIndex, sliceSize, data);
            sliceInfo.sliceSavePiece(sliceIndex, pieceIndex);
            sliceInfo.sliceHasAllPiece(sliceIndex);

            return true;
        }

        FileAndSlice fileAndSlice = new FileAndSlice(fileId, sliceIndex);
        SliceCacheContainer container = cache.get(fileAndSlice);
        if (container == null) {
            container = new SliceCacheContainer(sliceSize);
            container.addPiece(pieceIndex, data);
            currentMemoryCost += PIECE_SIZE;
        } else {
            boolean isFull = container.addPiece(pieceIndex, data);
            if (isFull) {
                byte[] dataForWrite = container.getWholeSlice();
                saveService.writeSliceData(fileAbsolutePath, sliceIndex, sliceSize, dataForWrite);
                currentMemoryCost -= sliceSize;
                sliceInfo.setSliceState(sliceIndex, SliceStateEnum.HAVING);
                return true;
            } else {
                container.addPiece(pieceIndex, data);
                currentMemoryCost += PIECE_SIZE;
            }
        }
        return false;
    }

    //如果该文件的slice状态是已经部分写入硬盘，则不再走缓存，直接写硬盘。
    //如果缓存区已经满了，也直接写硬盘，并且记录该slice状态
    private boolean shouldWriteDirectly(HashCode160 fileId, int sliceIndex, int sliceSize) {
        SliceStateEnum sliceState = fileInfoService.getFileData(fileId).getSlices().getSliceState(sliceIndex);

        return SliceStateEnum.PARTLY.equals(sliceState) ||
                currentMemoryCost + sliceSize > maxMemoryCost;
    }
}
