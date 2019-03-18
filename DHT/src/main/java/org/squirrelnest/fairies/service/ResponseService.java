package org.squirrelnest.fairies.service;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.dto.AbstractResult;
import org.squirrelnest.fairies.dto.FindNodeResult;
import org.squirrelnest.fairies.dto.FindValueResult;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.dto.PingResult;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.kvpairs.file.FileIndexContainer;
import org.squirrelnest.fairies.kvpairs.keyword.KeywordIndexContainer;
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
        return result;
    }

    /*
     *   STORE
     */

    /*
     *   PING
     */
    public PingResult ping(HashCode160 clientId, String clientIp, String clientPort) {
        routerTable.knowNode(clientId, clientIp, clientPort, true);

        return new PingResult();
    }
}
