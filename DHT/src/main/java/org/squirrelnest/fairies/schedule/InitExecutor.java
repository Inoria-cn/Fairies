package org.squirrelnest.fairies.schedule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.kvpairs.file.FileIndexContainer;
import org.squirrelnest.fairies.kvpairs.keyword.KeywordIndexContainer;
import org.squirrelnest.fairies.procedure.NodeJoin;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.LocalNodeService;
import org.squirrelnest.fairies.service.RequestSendService;
import org.squirrelnest.fairies.thread.GlobalThreadService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 当系统启动完毕后，如果路由表为空，表示节点是第一次加入系统，此时需要初始化构建路由表，同时获取本节点应存储的数据
 * Created by Inoria on 2019/3/20.
 */
@Component
class InitExecutor {

    @Value("${fairies.dht.k}")
    private Integer k;

    @Value("${fairies.dht.alpha}")
    private Integer alpha;

    @Value("${fairies.dht.request.timeout}")
    private Integer timeout;

    @Resource
    private RouterTable routerTable;

    @Resource
    private FileIndexContainer fileIndexContainer;

    @Resource
    private KeywordIndexContainer keywordIndexContainer;

    @Resource
    private LocalNodeService localNodeService;

    @Resource
    private RequestSendService requestSendService;

    @Resource
    private GlobalThreadService globalThreadService;

    @PostConstruct
    private void initDHTSystem() {
        if(!routerTable.isEmpty()) {
            return;
        }
        Record starter = localNodeService.getStarterNode();
        NodeJoin nodeJoin = new NodeJoin(starter, k, alpha, timeout, 50,
                routerTable, requestSendService, fileIndexContainer, keywordIndexContainer);
        globalThreadService.makeItRun(() -> {
            nodeJoin.execute();
        });
    }
}
