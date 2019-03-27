package org.squirrelnest.fairies.procedure;

import org.apache.commons.collections4.MapUtils;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.RequestSendService;
import org.squirrelnest.fairies.thread.CallableWithInform;
import org.squirrelnest.fairies.thread.inform.ResponseCountInform;
import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Inoria on 2019/3/19.
 */
public class Store extends AbstractProcedure<Map<HashCode160, Boolean>> {

    private final ExecutorService threadPool;
    private final KVValueTypeEnum typeEnum;

    private final List<Record> storeTargetNodes;
    private final Map<HashCode160, Long> id2ExpireTime;
    private final Object data;
    private final HashCode160 keyId;
    private final String keyword;

    Store(int k, int alpha, int requestTimeoutMs, KVValueTypeEnum typeEnum,
                 List<Record> targets, Map<HashCode160, Long> expireTimeMap, Object data, HashCode160 keyId, String keyword,
                 RouterTable routerTable, RequestSendService sendService) {
        super(null, k, alpha, requestTimeoutMs, routerTable, sendService);
        this.typeEnum = typeEnum;
        this.storeTargetNodes = targets;
        this.id2ExpireTime = expireTimeMap == null ? new HashMap<>(4) : expireTimeMap;
        this.data = data;
        this.keyId = keyId;
        this.keyword = keyword;

        this.result = new HashMap<>(targets.size());

        this.threadPool = new ThreadPoolExecutor(this.alpha, this.alpha * 4,
                3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    public Boolean isAllSuccess() {
        if (MapUtils.isEmpty(this.result)) {
            return null;
        }
        for(Map.Entry<HashCode160, Boolean> entry : this.result.entrySet()) {
            if(!entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Map<HashCode160, Boolean> execute() {
        Inform<Integer> informer = new ResponseCountInform();
        for(Record server : storeTargetNodes) {
            storeOneNode(server, informer);
        }
        blockUntilAllResponse(informer, storeTargetNodes.size(), requestTimeoutMs * 2);
        threadPool.shutdownNow();
        for(Record server : storeTargetNodes) {
            if(!this.result.containsKey(server.getNodeId())) {
                this.result.put(server.getNodeId(), false);
            }
        }
        return this.result;
    }

    private void storeOneNode(Record node, Inform<Integer> informer) {
        CallableWithInform<Boolean, Integer> task = new CallableWithInform<Boolean, Integer>() {
            @Override
            public Boolean originCall() throws Exception {

                Long expireTime = id2ExpireTime.get(node.getNodeId());
                Boolean queryResult = requestSendService.requestStore(node, keyId, data, typeEnum, keyword, expireTime);
                result.put(node.getNodeId(), queryResult);
                return queryResult;
            }
        };
        task.setInform(informer);
        threadPool.submit(task);
    }

    static void blockUntilAllResponse(Inform<Integer> informer, int totalCount, int requestTimeoutMs) {
        informer.blockUntilState(totalCount, (long)requestTimeoutMs);
    }
}
