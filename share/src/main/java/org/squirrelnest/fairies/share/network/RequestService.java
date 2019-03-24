package org.squirrelnest.fairies.share.network;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.network.HttpRequestSender;
import org.squirrelnest.fairies.share.dto.FileInfoDTO;
import org.squirrelnest.fairies.share.dto.FileSliceHashDTO;
import org.squirrelnest.fairies.share.dto.FileTransferDTO;
import org.squirrelnest.fairies.utils.MapBuilder;

import javax.annotation.Resource;

/**
 * Created by Inoria on 2019/3/21.
 */
@Service
public class RequestService {

    private static final String PATH_FILE_INFO = "download/fileData";
    private static final String PATH_PIECE_DATA = "download/piece";
    private static final String PATH_SLICE_HASH = "download/sliceHash";


    @Resource
    private HttpRequestSender httpRequestSender;

    private String sendGetRequest(Record server, String path, MapBuilder<String, String> builder) {
        String targetPath = server.getNodeIp() + ":" + server.getNodePort() + path;
        return httpRequestSender.httpGet(targetPath, builder.build());
    }

    public FileInfoDTO requestFileInfo(Record server, HashCode160 fileId) {
        String rawResult = sendGetRequest(server, PATH_FILE_INFO,
                new MapBuilder<String, String>().addField("fileId", fileId.toString()));
        return JSON.parseObject(rawResult, FileInfoDTO.class);
    }

    public FileTransferDTO requestPieceData(Record server, HashCode160 fileId, Integer sliceIndex, Integer pieceIndex) {
        String rawResult = sendGetRequest(server, PATH_PIECE_DATA, new MapBuilder<String, String>().
                addField("fileId", fileId.toString()).
                addField("sliceIndex", sliceIndex.toString()).
                addField("pieceIndex", pieceIndex.toString())
        );
        return JSON.parseObject(rawResult, FileTransferDTO.class);
    }

    public FileSliceHashDTO requestSliceHash(Record server, HashCode160 fileId, Integer sliceIndex) {
        String rawResult = sendGetRequest(server, PATH_PIECE_DATA, new MapBuilder<String, String>().
                addField("fileId", fileId.toString()).
                addField("sliceIndex", sliceIndex.toString())
        );
        return JSON.parseObject(rawResult, FileSliceHashDTO.class);
    }
}
