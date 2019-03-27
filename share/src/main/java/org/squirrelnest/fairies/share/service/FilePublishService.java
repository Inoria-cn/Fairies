package org.squirrelnest.fairies.share.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.common.exception.DuplicatedFileException;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.facade.DHTRequestFacade;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
import org.squirrelnest.fairies.local.domain.FileMetadata;
import org.squirrelnest.fairies.local.domain.SliceDetail;
import org.squirrelnest.fairies.local.enumeration.FileStateEnum;
import org.squirrelnest.fairies.local.service.LocalFileInfoService;
import org.squirrelnest.fairies.service.ConfigReadService;
import org.squirrelnest.fairies.service.LocalNodeService;

import javax.annotation.Resource;
import java.io.*;
import java.security.MessageDigest;
import java.util.List;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * Created by Inoria on 2019/3/24.
 */
@Service
public class FilePublishService {

    @Resource(name = "localFileInfoService")
    private LocalFileInfoService fileInfoService;

    @Resource
    private DHTRequestFacade dhtRequestFacade;

    @Resource
    private ConfigReadService configReadService;

    @Resource
    private LocalNodeService localNodeService;

    public void filePublish(String filePath, String name, String author, List<String> keywords) throws Exception {
        File file = new File(filePath);
        if (file.exists()) {
            throw new FileNotFoundException();
        }
        long fileSize = file.getTotalSpace();

        HashCode160 fileId = getFileId(file);
        if (fileInfoService.fileExist(fileId)) {
            throw new DuplicatedFileException();
        }
        FileMetadata fileMetadata = new FileMetadata();
        int sliceSize = getSliceSize(fileSize);
        fileMetadata.setSliceSize(sliceSize);
        fileMetadata.setId(fileId);
        fileMetadata.setPath(filePath);
        fileMetadata.setName(name);
        fileMetadata.setState(FileStateEnum.MY_OWN_FILE);
        fileMetadata.setSlices(new SliceDetail((int)((fileSize - 1) / sliceSize + 1), sliceSize));

        fileInfoService.putFileData(fileId, fileMetadata);
        FileValue fileValueForDHT = fileMetadata.prepareFileValueForDHT((int)fileSize, localNodeService.getStarterNode(),
                configReadService.getDHTKVValueExpireTime() + System.currentTimeMillis(), keywords);
        dhtRequestFacade.publishFileInfo(fileId, fileValueForDHT, author, file.lastModified());
    }

    private HashCode160 getFileId(File file) throws Exception {
        InputStream fileInputStream = new FileInputStream(file);
        byte[] readerCache = new byte[1024 * 50];
        int readByteCounter;
        MessageDigest sha1Digest = DigestUtils.getSha1Digest();
        while (true) {
            readByteCounter = fileInputStream.read(readerCache);
            if (readByteCounter <= 0) {
                break;
            }
            sha1Digest.update(readerCache);
        }
        return new HashCode160(sha1Digest.digest());
    }

    /**
     * 根据文件大小计算分片大小
     *
     * 计算规则：取值必须为2的n次方乘PIECE_SIZE，最大不超过16MB
     * 在满足上述条件的前提下，使分片数在100左右
     * @param fileSize 文件的总大小
     * @return 分片大小(Byte)
     */
    private int getSliceSize(long fileSize) {
        int sliceSize = PIECE_SIZE;
        while(true) {
            if (sliceSize * 100 > fileSize || sliceSize >= 16 * 1024 * 1024) {
                return sliceSize;
            }
            sliceSize *= 2;
        }
    }
}
