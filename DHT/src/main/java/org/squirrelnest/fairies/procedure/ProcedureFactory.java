package org.squirrelnest.fairies.procedure;

import org.springframework.stereotype.Component;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.kvpairs.file.FileIndexContainer;
import org.squirrelnest.fairies.kvpairs.keyword.KeywordIndexContainer;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.ConfigReadService;
import org.squirrelnest.fairies.service.LocalNodeService;
import org.squirrelnest.fairies.service.RequestSendService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/27.
 */
@Component
public class ProcedureFactory {

    @Resource
    private LocalNodeService localNodeService;

    @Resource
    private ConfigReadService configReadService;

    @Resource
    private RouterTable routerTable;

    @Resource
    private RequestSendService requestSendService;

    @Resource
    private FileIndexContainer fileIndexContainer;

    @Resource
    private KeywordIndexContainer keywordIndexContainer;


    private static ProcedureFactory self;

    @PostConstruct
    private void init() {
        self = this;
    }

    private ProcedureFactory() {

    }

    public static FindNode procedureFindNode(HashCode160 targetId) {
        return self.getFindNodeInstance(targetId);
    }

    public static FindValue procedureFindValue(HashCode160 targetId, KVValueTypeEnum typeEnum) {
        return self.getFindValueInstance(targetId, typeEnum);
    }

    public static NodeJoin procedureNodeJoin(Record starter) {
        return self.getNodeJoinInstance(starter);
    }

    public static Ping procedurePing(List<Record> targets) {
        return self.getPingInstance(targets);
    }

    public static Store procedureStoreValue(KVValueTypeEnum typeEnum, List<Record> targets, Map<HashCode160, Long> expireTimeMap,
                                            Object data, HashCode160 dataId) {
        return self.getStoreInstance(typeEnum, targets, expireTimeMap, data, dataId, null);
    }

    public static Store procedureStoreKeywordFile(List<Record> targets, Object data,
                                                  HashCode160 dataId, String keyword) {
        return self.getStoreInstance(KVValueTypeEnum.KEYWORD_FILE, targets, null, data, dataId, keyword);
    }



    private FindNode getFindNodeInstance(HashCode160 targetId) {
        return new FindNode(localNodeService.getLocalNodeId(), targetId,
                configReadService.getDHTParamK(), configReadService.getDHTParamAlpha(),
                configReadService.getDHTRequestTimeout(), routerTable, requestSendService);
    }

    private FindValue getFindValueInstance(HashCode160 targetId, KVValueTypeEnum typeEnum) {
        return new FindValue(targetId, configReadService.getDHTParamK(), configReadService.getDHTParamAlpha(),
                configReadService.getDHTRequestTimeout(), typeEnum, routerTable, requestSendService);
    }

    private NodeJoin getNodeJoinInstance(Record starter) {
        return new NodeJoin(starter, configReadService.getDHTParamK(),
                configReadService.getDHTParamAlpha(), configReadService.getDHTRequestTimeout(),
                routerTable, requestSendService, fileIndexContainer, keywordIndexContainer);
    }

    private Ping getPingInstance(List<Record> targets) {
        return new Ping(configReadService.getDHTParamK(), configReadService.getDHTParamAlpha(),
                configReadService.getDHTRequestTimeout(), targets, routerTable, requestSendService);
    }

    private Store getStoreInstance(KVValueTypeEnum typeEnum, List<Record> targets, Map<HashCode160, Long> expireTimeMap,
                                   Object data, HashCode160 dataId, String keyword) {
        return new Store(configReadService.getDHTParamK(), configReadService.getDHTParamAlpha(),
                configReadService.getDHTRequestTimeout(), typeEnum, targets, expireTimeMap,
                data, dataId, keyword, routerTable, requestSendService);
    }

}
