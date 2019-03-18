package org.squirrelnest.fairies.service;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.dto.FindNodeResult;
import org.squirrelnest.fairies.dto.FindValueResult;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.network.HttpRequestSender;
import org.squirrelnest.fairies.procedure.FindNode;
import org.squirrelnest.fairies.utils.ParamMapBuilder;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/16.
 */
@Service
public class RequestSendService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestSendService.class);

    @Resource
    private LocalNodeService localNodeService;

    @Resource
    private HttpRequestSender httpRequestSender;

    private String getTargetAddress(Record record) {
        return record.getNodeIp() + ":" + record.getNodePort();
    }

    public List<Record> requestNearestNodes(Record server, HashCode160 targetId) {
        Map<String, String> params = new ParamMapBuilder<String, String>().
                addFields(localNodeService.getLocalAddressParams()).
                addField("targetId", targetId.toString()).
                build();
        String rawResult = httpRequestSender.getRequest(getTargetAddress(server), params);
        FindNodeResult queryResult = JSON.parseObject(rawResult, FindNodeResult.class);
        if(!queryResult.success()) {
            LOGGER.error("Response object is not success, return code is " + queryResult.getReturnCode());
        }
        return queryResult.getNearerNodes();
    }

    public FindValueResult requestFindValue(Record server, HashCode160 targetId, KVValueTypeEnum typeEnum) {
        Map<String, String> params = new ParamMapBuilder<String, String>().
                addFields(localNodeService.getLocalAddressParams()).
                addField("targetId", targetId.toString()).
                addField("type", typeEnum.getValue()).
                build();
        String rawResult = httpRequestSender.getRequest(getTargetAddress(server), params);
        FindValueResult queryResult = JSON.parseObject(rawResult, FindValueResult.class);
        if(!queryResult.success()) {
            LOGGER.error("Response object is not success, return code is " + queryResult.getReturnCode());
        }
        return queryResult;
    }
}
