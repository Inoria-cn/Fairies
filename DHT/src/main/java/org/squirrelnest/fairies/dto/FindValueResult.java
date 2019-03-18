package org.squirrelnest.fairies.dto;

import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.dto.AbstractResult;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;

import java.util.List;

/**
 * Created by Inoria on 2019/3/17.
 */
public class FindValueResult<T> extends AbstractResult {

    private Boolean valueFound;
    private List<Record> nearerNodes;
    private KVValueTypeEnum typeEnum;
    private T value;

    public FindValueResult() {
        super();
    }

    public FindValueResult(FindNodeResult nodeResult) {
        super();
        valueFound = false;
        setNearerNodes(nodeResult.getNearerNodes());
    }

    public Boolean getValueFound() {
        return valueFound;
    }

    public void setValueFound(Boolean valueFound) {
        this.valueFound = valueFound;
    }

    public List<Record> getNearerNodes() {
        return nearerNodes;
    }

    public void setNearerNodes(List<Record> nearerNodes) {
        setReturnCode(RETURN_CODE_VALUE_NOT_FOUNT);
        this.nearerNodes = nearerNodes;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public KVValueTypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(KVValueTypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }
}
