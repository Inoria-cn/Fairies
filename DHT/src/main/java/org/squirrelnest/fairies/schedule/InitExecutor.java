package org.squirrelnest.fairies.schedule;

import org.springframework.stereotype.Component;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.procedure.NodeJoin;
import org.squirrelnest.fairies.procedure.ProcedureFactory;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.LocalNodeService;
import org.squirrelnest.fairies.service.GlobalThreadService;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 当系统启动完毕后，如果路由表为空，表示节点是第一次加入系统，此时需要初始化构建路由表，同时获取本节点应存储的数据
 * Created by Inoria on 2019/3/20.
 */
@Component
class InitExecutor {

    @Resource
    private RouterTable routerTable;

    @Resource
    private LocalNodeService localNodeService;

    @Resource
    private GlobalThreadService globalThreadService;

    @PostConstruct
    private void initDHTSystem() {
        if(!routerTable.isEmpty()) {
            return;
        }
        Record starter = localNodeService.getStarterNode();
        NodeJoin nodeJoin = ProcedureFactory.procedureNodeJoin(starter);
        globalThreadService.makeItRun(nodeJoin::execute);
    }
}
