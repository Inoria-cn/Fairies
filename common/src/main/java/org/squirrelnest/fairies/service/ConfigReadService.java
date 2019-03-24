package org.squirrelnest.fairies.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by Inoria on 2019/3/22.
 */
@Service
public class ConfigReadService {

    public static final int PIECE_SIZE = 16 * 1024;

    @Value("${fairies.dht.expireTime}")
    private Long dhtKVValueExpireTime;

    @Value("${fairies.dht.k}")
    private Integer dhtK;

    @Value("${fairies.dht.alpha}")
    private Integer dhtAlpha;

    @Value("${fairies.dht.request.timeout}")
    private Integer dhtRequestTimeout;

    @Value("${fairies.download.threadCount}")
    private Integer downloadThreadCount;

    @Value("${fairies.download.multiTask}")
    private Integer downloadMultiTaskCount;

    @Value("${fairies.download.pieceTimeout}")
    private Integer downloadPieceTimeout;

    @Value("${fairies.download.maxRetryTimes}")
    private Integer downloadMaxRetryTimes;


    public Long getDHTKVValueExpireTime() {
        return dhtKVValueExpireTime;
    }

    public Integer getDHTParamK() {
        return dhtK;
    }

    public Integer getDHTParamAlpha() {
        return dhtAlpha;
    }

    public Integer getDHTRequestTimeout() {
        return dhtRequestTimeout;
    }

    public Integer getDownloadThreadCount() {
        return downloadThreadCount;
    }

    public Integer getDownloadMultiTaskCount() {
        return downloadMultiTaskCount;
    }

    public Integer getDownloadPieceTimeout() {
        return downloadPieceTimeout;
    }

    public Integer getDownloadMaxRetryTimes() {
        return downloadMaxRetryTimes;
    }
}
