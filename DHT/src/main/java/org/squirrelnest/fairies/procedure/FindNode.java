package org.squirrelnest.fairies.procedure;

import org.squirrelnest.fairies.decorator.Decorator;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.procedure.interfaces.DHTProcedure;
import org.squirrelnest.fairies.router.Record;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.RequestSendService;
import org.squirrelnest.fairies.thread.CallableWithInform;
import org.squirrelnest.fairies.thread.inform.MessageGetInform;
import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Inoria on 2019/3/16.
 */
public class FindNode implements DHTProcedure<List<Record>> {

    private final String DECORATE_KEY_REQUEST = "requested";
    private final String DECORATE_KEY_RESPONSE = "response";

    private final HashCode160 localId;
    private final HashCode160 targetId;
    private final int k;
    private final int requestTimeoutMs;
    private final RouterTable routerTable;
    private final RequestSendService requestSendService;
    private final ExecutorService threadPool;

    private List<Record> startNodes;
    private Set<Decorator<Record>> proceedNodes = new HashSet<>(16);
    private List<Record> result = new ArrayList<>(16);
    private int lastMinDistance;

    public FindNode(HashCode160 localId, HashCode160 targetId, int k, int alpha, int requestTimeoutMs,
                    RouterTable routerTable, RequestSendService sendService) {
        this.localId = localId;
        this.targetId = targetId;
        this.k = k;
        this.requestTimeoutMs = requestTimeoutMs;
        this.routerTable = routerTable;
        this.requestSendService = sendService;

        startNodes = routerTable.getNearNodes(targetId);
        threadPool = new ThreadPoolExecutor(alpha, alpha * 4,
                60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * 查询逻辑：从初始节点开始并发查询，每当有结果返回时，将到目标的距离小于初始节点的节点们加入查询池，并记录结果中的最小距离
     * 从池中再挑选出alpha个未发送过请求的节点进行查询，只保存距离小于上一轮获得的最小距离的节点
     * 当池中节点全部已收到回复或未响应后，返回k个最近的节点
     */

    private Set<Decorator<Record>> getNoRequestNodes() {
        //For thread safe.
        Set<Decorator<Record>> view = new HashSet<>(proceedNodes);
        Set<Decorator<Record>> result = new HashSet<>(16);
        for(Decorator<Record> decorator : view) {
            Long requestTime = (Long)decorator.get(DECORATE_KEY_REQUEST);
            if(requestTime == null) {
                result.add(decorator);
            }
        }
        return result;
    }

    private Set<Decorator<Record>> getNoResponseNodes() {
        //For thread safe.
        Set<Decorator<Record>> view = new HashSet<>(proceedNodes);
        Set<Decorator<Record>> result = new HashSet<>(16);
        Long currentTime = System.currentTimeMillis();
        for(Decorator<Record> decorator : view) {
            Boolean hasResponse = (Boolean)decorator.getOrDefault(DECORATE_KEY_RESPONSE, false);
            Long requestTime = (Long)decorator.getOrDefault(DECORATE_KEY_REQUEST, currentTime);
            if(!hasResponse && currentTime - requestTime <= requestTimeoutMs) {
                result.add(decorator);
            }
        }
        return result;
    }

    private int calculateMinDistance(Collection<Decorator<Record>> range, HashCode160 target) {
        int minDistance = 161;
        for(Decorator<Record> record : range) {
            int distance = record.getData().getNodeId().calculateDistance(target);
            if(distance < minDistance) {
                minDistance = distance;
            }
        }
        return minDistance;
    }

    @Override
    public List<Record> execute() {
        Inform<Boolean> informer = new MessageGetInform();
        proceedNodes = Decorator.decoratorSet(new HashSet<>(startNodes));
        while(true) {
            lastMinDistance = calculateMinDistance(proceedNodes, targetId);
            Set<Decorator<Record>> noRequestNodes = getNoRequestNodes();
            if (noRequestNodes.isEmpty() && getNoResponseNodes().isEmpty()) {
                break;
            }
            for (Decorator<Record> record : noRequestNodes) {
                queryOneNode(record, informer);
            }
            blockUntilOneResponse(informer);
        }
        //此时还没有返回的目标节点，视为连接失效
        threadPool.shutdownNow();
        //获取结果中k个离目标最近的节点，返回，至此完成了findNode过程。
    }

    private void blockUntilOneResponse(Inform<Boolean> informer) {
         informer.setState(false);
         informer.blockUntilState(true, (long)requestTimeoutMs);
    }

    private void queryOneNode(Decorator<Record> receiver, Inform<Boolean> informer) {
        if (receiver.getData().getNodeId().equals(localId)) {
            return;
        }
        CallableWithInform<List<Record>, Boolean> task = new CallableWithInform<List<Record>, Boolean>() {
            @Override
            public List<Record> originCall() throws Exception {
                routerTable.requestNode(receiver.getData().getNodeId());
                List<Record> queryResult = requestSendService.requestNearestNodes(receiver.getData(), targetId);
                routerTable.knowNode(receiver.getData(), true);
                routerTable.knowNodes(queryResult, false);
                List<Record> filtered = filterDistance(queryResult);
                proceedNodes.addAll(Decorator.decoratorSet(new HashSet<>(filtered)));
                return queryResult;
            }
        };
        task.setInform(informer);
        threadPool.submit(task);
    }

    private List<Record> filterDistance(List<Record> source) {
        List<Record> result = new ArrayList<>(source.size());
        for (Record record : source) {
            if (record.getNodeId().calculateDistance(targetId) < lastMinDistance) {
                result.add(record);
            }
        }
        return result;
    }
}
