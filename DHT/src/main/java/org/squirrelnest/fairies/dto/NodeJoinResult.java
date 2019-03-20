package org.squirrelnest.fairies.dto;

import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
import org.squirrelnest.fairies.kvpairs.keyword.model.KeywordValue;

import java.util.List;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/20.
 */
public class NodeJoinResult extends AbstractResult {

    private List<Record> nearNodes;
    private Map<HashCode160, FileValue> fileKV;
    private Map<HashCode160, KeywordValue> keywordKV;

    public List<Record> getNearNodes() {
        return nearNodes;
    }

    public void setNearNodes(List<Record> nearNodes) {
        this.nearNodes = nearNodes;
    }

    public Map<HashCode160, FileValue> getFileKV() {
        return fileKV;
    }

    public void setFileKV(Map<HashCode160, FileValue> fileKV) {
        this.fileKV = fileKV;
    }

    public Map<HashCode160, KeywordValue> getKeywordKV() {
        return keywordKV;
    }

    public void setKeywordKV(Map<HashCode160, KeywordValue> keywordKV) {
        this.keywordKV = keywordKV;
    }
}
