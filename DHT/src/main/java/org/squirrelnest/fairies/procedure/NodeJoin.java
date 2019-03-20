package org.squirrelnest.fairies.procedure;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.squirrelnest.fairies.decorator.Decorator;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.dto.NodeJoinResult;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.kvpairs.file.FileIndexContainer;
import org.squirrelnest.fairies.kvpairs.keyword.KeywordIndexContainer;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.RequestSendService;
import org.squirrelnest.fairies.thread.CallableWithInform;
import org.squirrelnest.fairies.thread.inform.OneResponseInform;
import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.squirrelnest.fairies.procedure.FindNode.*;

/**
 * Created by Inoria on 2019/3/20.
 */
public class NodeJoin extends AbstractProcedure {

    private final KeywordIndexContainer keywordContainer;
    private final FileIndexContainer fileContainer;
    private final ExecutorService threadPool;
    private final int maxRequestNumber;
    private int requestCounter = 0;

    private final Record startNode;
    private Set<Decorator<Record>> proceedNodes = new HashSet<>(16);

    public NodeJoin(Record startNode, int k, int alpha, int requestTimeoutMs, int maxRequestNumber,
                    RouterTable routerTable, RequestSendService sendService,
                    FileIndexContainer fileIndexContainer, KeywordIndexContainer keywordIndexContainer) {
        super(null, k, alpha, requestTimeoutMs, routerTable, sendService);
        this.keywordContainer = keywordIndexContainer;
        this.fileContainer = fileIndexContainer;
        this.startNode = startNode;
        this.maxRequestNumber = maxRequestNumber;

        this.threadPool = new ThreadPoolExecutor(this.alpha, this.alpha * 4,
                3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * 初始化逻辑：不断向池中还未发送请求的节点发送NODE_JOIN请求，直到没有此类节点或者达到发送请求数目的上限
     * 每当接收到返回结果时，添加进路由表，kv数据等。
     */
    @Override
    public Object execute() {
        Inform<Boolean> informer = new OneResponseInform();
        proceedNodes.addAll(Decorator.decorateList(Arrays.asList(startNode)));
        while (true) {
            Set<Decorator<Record>> noRequestNodes = getNoRequestNodes(proceedNodes);
            if (requestCounter > maxRequestNumber) {
                break;
            }
            if (noRequestNodes.isEmpty() && getNoResponseNodes(proceedNodes, requestTimeoutMs).isEmpty()) {
                break;
            }
            for (Decorator<Record> record : noRequestNodes) {
                queryOneNode(record, informer);
            }
            blockUntilOneResponse(informer, requestTimeoutMs);
        }

        threadPool.shutdownNow();

        return null;
    }

    private void queryOneNode(Decorator<Record> node, Inform<Boolean> informer) {
        CallableWithInform<Object, Boolean> task = new CallableWithInform<Object, Boolean>() {

            @Override
            public Object originCall() {
                node.put(DECORATE_KEY_REQUEST, System.currentTimeMillis());
                requestCountAdd();

                NodeJoinResult queryResult = requestSendService.requestNodeJoin(node.getData());
                node.put(DECORATE_KEY_RESPONSE, true);

                proceedNodes.addAll(Decorator.decorateList(queryResult.getNearNodes()));
                routerTable.knowNodes(queryResult.getNearNodes(), false);
                keywordContainer.refreshPutAll(queryResult.getKeywordKV());
                fileContainer.refreshPutAll(queryResult.getFileKV());
                return null;
            }
        };
        task.setInform(informer);
        threadPool.submit(task);
    }

    private synchronized void requestCountAdd() {
        this.requestCounter++;
    }
}
