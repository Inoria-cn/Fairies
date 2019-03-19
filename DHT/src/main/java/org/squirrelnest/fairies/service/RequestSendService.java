package org.squirrelnest.fairies.service;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.dto.FindNodeResult;
import org.squirrelnest.fairies.dto.FindValueResult;
import org.squirrelnest.fairies.dto.PingResult;
import org.squirrelnest.fairies.dto.StoreResult;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.network.HttpRequestSender;
import org.squirrelnest.fairies.router.RouterTable;
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

    private static final String REQUEST_PATH_FIND_NODES = "/DHT/findNode";
    private static final String REQUEST_PATH_FIND_VALUE = "/DHT/findValue";
    private static final String REQUEST_PATH_STORE = "/DHT/store";
    private static final String REQUEST_PATH_PING = "/DHT/ping";


    @Resource
    private LocalNodeService localNodeService;

    @Resource
    private HttpRequestSender httpRequestSender;

    @Resource
    private RouterTable routerTable;

    private String sendGetRequest(Record record, String path, ParamMapBuilder<String, String> paramBuilder) {
        routerTable.requestNode(record.getNodeId());
        String requestUrl = record.getNodeIp() + ":" + record.getNodePort() + path;
        String queryResult = httpRequestSender.httpGet(requestUrl, paramBuilder.build());
        routerTable.knowNode(record, true);
        return queryResult;
    }

    public List<Record> requestNearestNodes(Record server, HashCode160 targetId) {
        ParamMapBuilder<String, String> builder = new ParamMapBuilder<String, String>().
                addFields(localNodeService.getLocalAddressParams()).
                addField("targetId", targetId.toString());
        String rawResult = sendGetRequest(server, REQUEST_PATH_FIND_NODES , builder);
        FindNodeResult queryResult = JSON.parseObject(rawResult, FindNodeResult.class);
        if(!queryResult.success()) {
            LOGGER.error("Response object is not success, return code is " + queryResult.getReturnCode());
        }
        return queryResult.getNearerNodes();
    }

    public FindValueResult requestFindValue(Record server, HashCode160 targetId, KVValueTypeEnum typeEnum) {
        ParamMapBuilder<String, String> builder = new ParamMapBuilder<String, String>().
                addFields(localNodeService.getLocalAddressParams()).
                addField("targetId", targetId.toString()).
                addField("type", typeEnum.getValue());
        String rawResult = sendGetRequest(server, REQUEST_PATH_FIND_VALUE, builder);
        FindValueResult queryResult = JSON.parseObject(rawResult, FindValueResult.class);
        if(!queryResult.success()) {
            LOGGER.error("Response object is not success, return code is " + queryResult.getReturnCode());
        }
        return queryResult;
    }

    /**
     * @param expireTime 该字段如果不为null，会优先覆盖对应value中的过期时间配置，用于按照距离不同进行缓存
     * @return 是否成功
     */
    public Boolean requestStore(Record server, HashCode160 key, Object value,
                                KVValueTypeEnum typeEnum, String keyword, Long expireTime) {
        ParamMapBuilder<String, String> builder = new ParamMapBuilder<String, String>().
                addFields(localNodeService.getLocalAddressParams()).
                addField("key", key.toString()).
                addField("value", JSON.toJSONString(value)).
                addField("type", typeEnum.getValue());
        if (typeEnum.equals(KVValueTypeEnum.KEYWORD_FILE)) {
            builder.addField("keyword", keyword);
        } else {
            builder.addField("expireTime", expireTime.toString());
        }
        String rawResult = sendGetRequest(server, REQUEST_PATH_STORE, builder);
        StoreResult queryResult = JSON.parseObject(rawResult, StoreResult.class);
        if(!queryResult.success()) {
            LOGGER.error("Response object of store request is not success, return code is " + queryResult.getReturnCode());
        }
        return queryResult.success();
    }

    public Boolean requestPing(Record server) {
        ParamMapBuilder<String, String> builder = new ParamMapBuilder<String, String>().
                addFields(localNodeService.getLocalAddressParams());
        String rawResult = sendGetRequest(server, REQUEST_PATH_PING, builder);
        PingResult queryResult = JSON.parseObject(rawResult, PingResult.class);
        if(!queryResult.success()) {
            LOGGER.error("Response object of ping request is not success, return code is " + queryResult.getReturnCode());
        }
        return queryResult.success();
    }
}
