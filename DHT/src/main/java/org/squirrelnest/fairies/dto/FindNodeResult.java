package org.squirrelnest.fairies.dto;

import org.squirrelnest.fairies.domain.Record;

import java.util.List;

/**
 * Created by Inoria on 2019/3/17.
 */
public class FindNodeResult extends AbstractResult {

    private List<Record> nearerNodes;

    public FindNodeResult(List<Record> nodes) {
        super();
        this.nearerNodes = nodes;
    }

    public List<Record> getNearerNodes() {
        return nearerNodes;
    }

    public void setNearerNodes(List<Record> nearerNodes) {
        this.nearerNodes = nearerNodes;
    }

}
