package org.squirrelnest.fairies.share.service;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.network.HttpRequestSender;
import org.squirrelnest.fairies.share.dto.FileInfoResult;
import org.squirrelnest.fairies.utils.MapBuilder;

import javax.annotation.Resource;

/**
 * Created by Inoria on 2019/3/21.
 */
@Service
public class RequestService {

    private static final String PATH_FILE_DATA = "download/fileData";

    @Resource
    private HttpRequestSender httpRequestSender;

    private String sendGetRequest(Record server, String path, MapBuilder<String, String> builder) {
        String targetPath = server.getNodeIp() + ":" + server.getNodePort() + path;
        return httpRequestSender.httpGet(targetPath, builder.build());
    }

    public FileInfoResult requestFileInfo(Record server, HashCode160 fileId) {
        String rawResult = sendGetRequest(server, PATH_FILE_DATA,
                new MapBuilder<String, String>().addField("fileId", fileId.toString()));
        return JSON.parseObject(rawResult, FileInfoResult.class);
    }
}
