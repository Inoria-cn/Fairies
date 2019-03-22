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
import org.squirrelnest.fairies.service.ConfigReadService;
import org.squirrelnest.fairies.service.LocalNodeService;
import org.squirrelnest.fairies.service.RequestSendService;
import org.squirrelnest.fairies.service.GlobalThreadService;

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

    @Resource
    private ConfigReadService configReadService;

    @Resource
    private RouterTable routerTable;

    @Resource
    private KeywordIndexContainer keywordIndexContainer;

    @Resource
    private RequestSendService requestSendService;

    @Resource
    private GlobalThreadService globalThreadService;

    @Resource
    private LocalNodeService localNodeService;

    public Record getLocalRecord() {
        return localNodeService.getLocalNodeRecord();
    }

    @SuppressWarnings("unchecked")
    public List<File> getFilesByKeyword(String keyword) {
        HashCode160 wordHash = HashCode160.newInstance(keyword);
        FindValue findValue = new FindValue(wordHash, configReadService.getDHTParamK()
                , configReadService.getDHTParamAlpha(), configReadService.getDHTRequestTimeout(),
                KVValueTypeEnum.KEYWORD, routerTable, requestSendService);
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
                Store cacheStore = new Store(configReadService.getDHTParamK(), configReadService.getDHTParamAlpha(),
                        configReadService.getDHTRequestTimeout(), KVValueTypeEnum.KEYWORD, cacheRecords,
                        expireTimeMap, value, wordHash, keyword, routerTable, requestSendService);
                cacheStore.execute();
            });
        }

        return new ArrayList<>(value.getId2File().values());
    }

    @SuppressWarnings("unchecked")
    public FileValue getFileInfoById(HashCode160 fileId, List<Record> nearestNodesContainer) {
        FindValue findValue = new FindValue(fileId, configReadService.getDHTParamK(), configReadService.getDHTParamAlpha(),
                configReadService.getDHTRequestTimeout(), KVValueTypeEnum.FILE, routerTable, requestSendService);
        FindValueResult result = findValue.execute();
        if (nearestNodesContainer != null) {
            nearestNodesContainer.addAll(result.getNearerNodes());
        }
        if(!result.getValueFound()) {
            return null;
        }

        return (FileValue)result.getValue();
    }

    public void asyncUpdateFileHolders(List<Record> targetNodes, FileValue oldValue, List<Record> newHolders) {
        globalThreadService.makeItRun(() -> {
            FileValue newValue = new FileValue(oldValue);
            newValue.setHolders(newHolders);
            newValue.setExpireTimestamp(System.currentTimeMillis() + configReadService.getDHTKVValueExpireTime());
            Store fileValueUpdate = new Store(configReadService.getDHTParamK(), configReadService.getDHTParamAlpha(),
                    configReadService.getDHTRequestTimeout(), KVValueTypeEnum.FILE, targetNodes,
                    null, newValue, oldValue.getId(), null, routerTable, requestSendService);
            fileValueUpdate.execute();
        });
    }
}
