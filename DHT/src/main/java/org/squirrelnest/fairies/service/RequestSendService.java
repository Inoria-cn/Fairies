package org.squirrelnest.fairies.service;

import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.router.Record;

import java.util.List;

/**
 * Created by Inoria on 2019/3/16.
 */
@Service
public class RequestSendService {

    public List<Record> requestNearestNodes(Record server, HashCode160 targetId) {
        return null;
    }
}
