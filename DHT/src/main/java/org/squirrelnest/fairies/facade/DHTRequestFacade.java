package org.squirrelnest.fairies.facade;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.dto.FindValueResult;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
import org.squirrelnest.fairies.kvpairs.keyword.KeywordIndexContainer;
import org.squirrelnest.fairies.kvpairs.keyword.model.File;
import org.squirrelnest.fairies.kvpairs.keyword.model.KeywordValue;
import org.squirrelnest.fairies.procedure.FindValue;
import org.squirrelnest.fairies.procedure.Store;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.RequestSendService;
import org.squirrelnest.fairies.thread.GlobalThreadService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 提供给文件下载模块一个操作DHT相关源数据的接口
 * Created by Inoria on 2019/3/18.
 */
@Service
public class DHTRequestFacade {

    @Value("${fairies.dht.k}")
    private Integer k;

    @Value("${fairies.dht.alpha}")
    private Integer alpha;

    @Value("${fairies.dht.request.timeout}")
    private Integer timeout;

    @Resource
    private RouterTable routerTable;

    @Resource
    private KeywordIndexContainer keywordIndexContainer;

    @Resource
    private RequestSendService requestSendService;

    @Resource
    private GlobalThreadService globalThreadService;

    @SuppressWarnings("unchecked")
    public List<File> getFilesByKeyword(String keyword) {
        HashCode160 wordHash = HashCode160.newInstance(keyword);
        FindValue findValue = new FindValue(wordHash, k, alpha, timeout, KVValueTypeEnum.KEYWORD,
                routerTable, requestSendService);
        findValue.execute();
        FindValueResult result = findValue.getTargetTypeResult(FindValueResult.class);
        if(!result.getValueFound()) {
            return new ArrayList<>(4);
        }
        KeywordValue value = (KeywordValue)result.getValue();
        List<Record> cacheRecords = result.getNearerNodes();
        if(CollectionUtils.isNotEmpty(cacheRecords)) {
            Map<HashCode160, Long> expireTimeMap = keywordIndexContainer.getExpireTimeMap(wordHash, cacheRecords);
            globalThreadService.makeItRun(() -> {
                Store cacheStore = new Store(k, alpha, timeout, KVValueTypeEnum.KEYWORD, cacheRecords,
                        expireTimeMap, value, wordHash, keyword, routerTable, requestSendService);
                cacheStore.execute();
            });
        }

        return new ArrayList<>(value.getId2File().values());
    }

    @SuppressWarnings("unchecked")
    public FileValue getFileInfoById(HashCode160 fileId, List<Record> nearestNodesContainer) {
        FindValue findValue = new FindValue(fileId, k, alpha, timeout, KVValueTypeEnum.FILE,
                routerTable, requestSendService);
        FindValueResult result = findValue.execute();
        nearestNodesContainer.addAll(result.getNearerNodes());
        if(!result.getValueFound()) {
            return null;
        }
        FileValue value = (FileValue)result.getValue();
        return value;
    }
}
