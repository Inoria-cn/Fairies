package org.squirrelnest.fairies.service;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.router.Record;
import org.squirrelnest.fairies.router.RouterTable;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Inoria on 2019/3/16.
 */
@Service
public class RequestHandleService {

    @Resource
    private RouterTable routerTable;

    public List<Record> findNode(HashCode160 clientId, String clientIp, String clientPort, HashCode160 target) {
        routerTable.knowNode(clientId, clientIp, clientPort, true);
        return routerTable.getNearNodes(target);
    }
}
