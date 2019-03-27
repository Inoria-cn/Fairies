package org.squirrelnest.fairies.procedure;

import org.squirrelnest.fairies.decorator.Decorator;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.dto.FindValueResult;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.RequestSendService;
import org.squirrelnest.fairies.thread.CallableWithInform;
import org.squirrelnest.fairies.thread.inform.OneResponseInform;
import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.squirrelnest.fairies.procedure.FindNode.*;

/**
 * 返回值复用了DTO，但是字段的含义发生了改变，nearNode表示应当进行缓存的节点。
 * 为了保证能获得最新的文件所有者列表，文件kv信息不进行缓存，关键词kv信息按距离进行时间不同的缓存
 *
 * Created by Inoria on 2019/3/16.
 */
public class FindValue extends AbstractProcedure<FindValueResult> {

    private final static String DECORATE_KEY_VALUE = "value";

    private final ExecutorService threadPool;
    private final KVValueTypeEnum typeEnum;

    private final List<Record> startNodes;
    private Set<Decorator<Record>> proceedNodes = new HashSet<>(16);
    private Decorator<Record> nodeWithValue = null;

    private int lastMinDistance;

    FindValue(HashCode160 targetId,
                     int k, int alpha, int requestTimeoutMs, KVValueTypeEnum typeEnum,
                     RouterTable routerTable, RequestSendService sendService) {
        super(targetId, k, alpha, requestTimeoutMs, routerTable, sendService);
        this.typeEnum = typeEnum;
        startNodes = routerTable.getNearNodes(targetId);
        threadPool = new ThreadPoolExecutor(this.alpha, this.alpha * 4,
                3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    public boolean isValueFound() {
        return super.result != null;
    }

    @SuppressWarnings("unchecked")
    public <T> FindValueResult<T> getTargetTypeResult(Class<T> aClass) {
        return (FindValueResult<T>)super.result;
    }

    /**
     * 查询逻辑：从初始节点开始并发查询，每当有结果返回时，将到目标的距离小于初始节点的节点们加入查询池，并记录结果中的最小距离
     * 从池中再挑选出alpha个未发送过请求的节点进行查询，只保存距离小于上一轮获得的最小距离的节点
     * 如果有节点返回了结果，中断查询过程。
     * 当池中节点全部已收到回复或未响应后，返回k个最近的节点
     */
    @Override
    @SuppressWarnings("unchecked")
    public FindValueResult execute() {

        Inform<Boolean> informer = new OneResponseInform();
        proceedNodes = Decorator.decoratorSet(new HashSet<>(startNodes));

        //多线程连续查询主要逻辑
        while(true) {
            lastMinDistance = calculateMinDistance(proceedNodes, targetId);
            Set<Decorator<Record>> noRequestNodes = getNoRequestNodes(proceedNodes);
            if (noRequestNodes.isEmpty() && getNoResponseNodes(proceedNodes, requestTimeoutMs).isEmpty()) {
                break;
            }
            for (Decorator<Record> record : noRequestNodes) {
                queryOneNode(record, informer);
            }
            blockUntilOneResponse(informer, requestTimeoutMs);
            //above lines in this while-loop is same as codes in FindNode.
            //If this value returned, break and kill all other threads.
            if (nodeWithValue != null) {
                break;
            }
        }

        //此时还没有返回的目标节点，视为连接失效
        threadPool.shutdownNow();

        super.result = new FindValueResult();

        //获取结果中k个离目标最近的节点
        List<Record> nearestRecords = findKNearestNodesFromProceed(proceedNodes, targetId, k);
        super.result.setNearerNodes(nearestRecords);

        if (nodeWithValue != null) {
            super.result.setValueFound(true);
            super.result.setValue(nodeWithValue.get(DECORATE_KEY_VALUE));
            nearestRecords.remove(nodeWithValue.getData());
        }

        return super.result;
    }

    @SuppressWarnings("unchecked")
    private void queryOneNode(Decorator<Record> receiver, Inform<Boolean> informer) {
        CallableWithInform<List<Record>, Boolean> task = new CallableWithInform<List<Record>, Boolean>() {
            @Override
            public List<Record> originCall() throws Exception {

                receiver.put(DECORATE_KEY_REQUEST, System.currentTimeMillis());

                FindValueResult queryResult = requestSendService.requestFindValue(receiver.getData(), targetId, typeEnum);

                receiver.put(DECORATE_KEY_RESPONSE, true);

                if (queryResult.getValueFound()) {
                    receiver.put(DECORATE_KEY_VALUE, queryResult.getValue());
                    nodeWithValue = receiver;
                    return null;
                } else {
                    List<Record> nearRecords = queryResult.getNearerNodes();
                    routerTable.knowNodes(nearRecords, false);
                    List<Record> filtered = filterNearerRecords(nearRecords, targetId, lastMinDistance);
                    proceedNodes.addAll(Decorator.decoratorSet(new HashSet<>(filtered)));
                    return nearRecords;
                }
            }
        };
        task.setInform(informer);
        threadPool.submit(task);
    }

}
