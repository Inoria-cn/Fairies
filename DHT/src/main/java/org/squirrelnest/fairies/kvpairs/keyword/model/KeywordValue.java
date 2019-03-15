package org.squirrelnest.fairies.kvpairs.keyword.model;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.exception.DHTMessageException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/13.
 */
public class KeywordValue {

    final static String FILE_MSG_KEY_KEYWORD = "keyword";

    private final static Logger LOGGER = LoggerFactory.getLogger(KeywordValue.class);

    private String keyword;
    private Map<HashCode160, File> id2File;
    private Long lastRepublicReceiveTime;

    public KeywordValue() {
        this.id2File = new HashMap<>(16);
    }

    public boolean addFile(File file) {
        if (!file.valid()) {
            LOGGER.error("file is invalid!");
            return false;
        }
        HashCode160 id = file.getId();
        id2File.put(id, file);
        return true;
    }

    public static KeywordValue createFromString(String raw) {
        Map<String, Object> map = JSON.parseObject(raw);
        String keyword = (String)map.get(FILE_MSG_KEY_KEYWORD);
        File newFile = File.getFromMap(map);
        if (StringUtils.isBlank(keyword) || !newFile.valid()) {
            throw new DHTMessageException("Can't read this keyword add message");
        }

        KeywordValue result = new KeywordValue();
        result.setKeyword(keyword);
        result.getId2File().put(newFile.getId(), newFile);
        return result;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Map<HashCode160, File> getId2File() {
        return id2File;
    }

    public void setId2File(Map<HashCode160, File> id2File) {
        this.id2File = id2File;
    }

    public Long getLastRepublicReceiveTime() {
        return lastRepublicReceiveTime;
    }

    public void setLastRepublicReceiveTime(Long lastRepublicReceiveTime) {
        this.lastRepublicReceiveTime = lastRepublicReceiveTime;
    }
}
