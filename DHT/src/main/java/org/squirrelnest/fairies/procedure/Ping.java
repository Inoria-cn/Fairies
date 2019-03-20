package org.squirrelnest.fairies.procedure;

import org.apache.commons.collections4.MapUtils;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
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

import static org.squirrelnest.fairies.procedure.Store.blockUntilAllResponse;

/**
 * Created by Inoria on 2019/3/19.
 */
public class Ping extends AbstractProcedure<Map<HashCode160, Boolean>> {

    private final ExecutorService threadPool;
    private final List<Record> targetNodes;

    public Ping(int k, int alpha, int requestTimeoutMs,
                List<Record> targets, RouterTable routerTable, RequestSendService sendService) {
        super(null, k, alpha, requestTimeoutMs, routerTable, sendService);
        this.targetNodes = targets;
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
        for(Record server : targetNodes) {
            pingOneNode(server, informer);
        }
        blockUntilAllResponse(informer, targetNodes.size(), requestTimeoutMs);
        threadPool.shutdownNow();
        for(Record server : targetNodes) {
            if(!this.result.containsKey(server.getNodeId())) {
                this.result.put(server.getNodeId(), false);
            }
        }
        return this.result;
    }

    private void pingOneNode(Record node, Inform<Integer> informer) {
        CallableWithInform<Boolean, Integer> task = new CallableWithInform<Boolean, Integer>() {
            @Override
            public Boolean originCall() throws Exception {
                Boolean queryResult = requestSendService.requestPing(node);
                result.put(node.getNodeId(), queryResult);
                return queryResult;
            }
        };
        task.setInform(informer);
        threadPool.submit(task);
    }
}
