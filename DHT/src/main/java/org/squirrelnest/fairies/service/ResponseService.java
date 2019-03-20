package org.squirrelnest.fairies.service;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.dto.*;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.kvpairs.file.FileIndexContainer;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
import org.squirrelnest.fairies.kvpairs.keyword.KeywordIndexContainer;
import org.squirrelnest.fairies.kvpairs.keyword.model.File;
import org.squirrelnest.fairies.kvpairs.keyword.model.KeywordValue;
import org.squirrelnest.fairies.router.RouterTable;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Inoria on 2019/3/16.
 */
@Service
public class ResponseService {

    @Resource
    private RouterTable routerTable;

    @Resource
    private FileIndexContainer fileIndexContainer;

    @Resource
    private KeywordIndexContainer keywordIndexContainer;

    /*
     *   FIND_NODE
     */
    public FindNodeResult findNode(HashCode160 clientId, String clientIp, String clientPort, HashCode160 target) {
        routerTable.knowNode(clientId, clientIp, clientPort, true);

        List<Record> nodes = routerTable.getNearNodes(target);
        return new FindNodeResult(nodes);
    }


    /*
     *   FIND_VALUE
     */
    public FindValueResult findValue(HashCode160 clientId, String clientIp, String clientPort, HashCode160 target, String valueType) {
        routerTable.knowNode(clientId, clientIp, clientPort, true);

        FindValueResult result;
        KVValueTypeEnum type = KVValueTypeEnum.find(valueType);
        switch (type) {
            case FILE:
                return findKvValue(clientId, clientIp, clientPort, target, true);
            case KEYWORD:
                return findKvValue(clientId, clientIp, clientPort, target, false);
            default:
                result = new FindValueResult(findNode(clientId, clientIp, clientPort, target));
                result.setReturnCode(AbstractResult.RETURN_CODE_PARAM_ERROR);
                result.setReturnMessage("Can't find kv value of type " + valueType);
                return result;
        }
    }

    @SuppressWarnings("unchecked")
    private FindValueResult findKvValue(HashCode160 clientId, String clientIp, String clientPort,
                                        HashCode160 target, boolean isFile) {
        FindValueResult result;
        Object kvValue = isFile ? fileIndexContainer.get(target) : keywordIndexContainer.get(target);
        if (kvValue == null) {
            result = new FindValueResult(findNode(clientId, clientIp, clientPort, target));
        } else {
            result = new FindValueResult();
            result.setValueFound(true);
            result.setValue(kvValue);
        }
        result.setTypeEnum(isFile ? KVValueTypeEnum.FILE : KVValueTypeEnum.KEYWORD);
        return result;
    }

    /*
     *   STORE
     */
    public StoreResult store(HashCode160 clientId, String clientIp, String clientPort,
                             HashCode160 key, String valueString, String type, String keyword, String expireTime) {
        routerTable.knowNode(clientId, clientIp, clientPort, true);

        Long expireTimeLong = expireTime == null ? null : Long.parseLong(expireTime);
        StoreResult result;
        KVValueTypeEnum typeEnum = KVValueTypeEnum.find(type);
        switch (typeEnum) {
            case KEYWORD:
                result = storeKeywordKVPair(key, valueString);
                break;
            case FILE:
                result = storeFileKVPair(key, valueString, expireTimeLong);
                break;
            case KEYWORD_FILE:
                result = addFile2KeywordKVPair(key, keyword, valueString, expireTimeLong);
                break;
            default:
                result = new StoreResult();
                result.setReturnCode(AbstractResult.RETURN_CODE_PARAM_ERROR);
                result.setReturnMessage("Can't find kv value of type " + type);
        }

        return result;
    }

    private StoreResult storeFileKVPair(HashCode160 key, String valueString, Long expireTime) {
        FileValue value = JSON.parseObject(valueString, FileValue.class);
        if (expireTime != null) {
            value.setExpireTimestamp(expireTime);
        }
        boolean success = fileIndexContainer.refreshPut(key, value);
        StoreResult result = new StoreResult();
        if(!success) {
            result.setReturnCode(AbstractResult.RETURN_CODE_FAILED);
            result.setReturnMessage("FileValue pair store failed, maybe your data version is not latest");
        }
        return result;
    }

    private StoreResult storeKeywordKVPair(HashCode160 key, String valueString) {
        KeywordValue value = JSON.parseObject(valueString, KeywordValue.class);
        boolean success = keywordIndexContainer.refreshPut(key, value);
        StoreResult result = new StoreResult();
        if(!success) {
            result.setReturnCode(AbstractResult.RETURN_CODE_FAILED);
            result.setReturnMessage("KeywordValue pair store failed, maybe your data version is not latest");
        }
        return result;
    }

    private StoreResult addFile2KeywordKVPair(HashCode160 key, String keyword, String valueString, Long expireTime) {
        File file = JSON.parseObject(valueString, File.class);
        if (expireTime != null) {
            file.setExpireTime(expireTime);
        }
        boolean success = keywordIndexContainer.addFile2Keyword(key, keyword, file);
        StoreResult result = new StoreResult();
        if(!success) {
            result.setReturnCode(AbstractResult.RETURN_CODE_FAILED);
            result.setReturnMessage("KeywordValue add file failed, file object is invalid for lack of fields");
        }
        return result;
    }

    /*
     *   PING
     */
    public PingResult ping(HashCode160 clientId, String clientIp, String clientPort) {
        routerTable.knowNode(clientId, clientIp, clientPort, true);

        return new PingResult();
    }

    /*
     *   NODE_JOIN
     */
    public NodeJoinResult nodeJoin(HashCode160 clientId, String clientIp, String clientPort) {
        NodeJoinResult result = new NodeJoinResult();
        result.setNearNodes(routerTable.getNearNodes(clientId));
        routerTable.knowNode(clientId, clientIp, clientPort, true);

        result.setFileKV(fileIndexContainer.getDataForNewNode(clientId));
        result.setKeywordKV(keywordIndexContainer.getDataForNewNode(clientId));
        return result;
    }
}
