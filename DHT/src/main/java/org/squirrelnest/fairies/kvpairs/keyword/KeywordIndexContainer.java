package org.squirrelnest.fairies.kvpairs.keyword;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.kvpairs.keyword.model.File;
import org.squirrelnest.fairies.kvpairs.keyword.model.KeywordValue;
import org.squirrelnest.fairies.service.LocalNodeService;
import org.squirrelnest.fairies.storage.datasource.interfaces.DataSource;
import org.squirrelnest.fairies.storage.enumeration.LocalStorageTypeEnum;
import org.squirrelnest.fairies.utils.TimeUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/13.
 */
@Component
public class KeywordIndexContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordIndexContainer.class);

    @Value("${fairies.dht.republish.cancelMs}")
    private Long leastRepublishMs;

    @Resource(name = "localStorageDAO")
    private DataSource localStorage;

    @Resource
    private LocalNodeService localNodeService;

    private Map<HashCode160, KeywordValue> keywordData;

    @PostConstruct
    private void init() {
        boolean loadResult = loadFromBackup();
        if (!loadResult || keywordData == null) {
            keywordData = new HashMap<>(16);
        }
    }

    @PreDestroy
    private void gentleDestroy() {
        backup();
    }

    public KeywordValue get(HashCode160 target) {
        return keywordData.get(target);
    }

    public boolean addFile2Keyword(HashCode160 keywordHash, String keyword, File target) {
        KeywordValue targetRecord;
        if(!keywordData.containsKey(keywordHash)) {
            targetRecord = new KeywordValue();
            keywordData.put(keywordHash, targetRecord);
            targetRecord.setKeyword(keyword);
        } else {
            targetRecord = keywordData.get(keywordHash);
        }
        return targetRecord.addFile(target);
    }

    /**
     * 收到刷新请求时，为了防止旧值通过重新发布覆盖了新值，需要判断条目最近更新时间。
     * 同时为了防止恶意发送更新时间极大的数据，当条目更新时间超过当前时间时，将该条刷新请求作废
     * @param keywordHash 要刷新的key
     * @param newValue 接收到的值
     */
    public boolean refreshPut(HashCode160 keywordHash, KeywordValue newValue) {
        KeywordValue oldValue = keywordData.get(keywordHash);
        if(oldValue == null) {
            keywordData.put(keywordHash, newValue);
            return true;
        }
        boolean newerAndValid = newValue.getLastFileUpdateTime() > oldValue.getLastFileUpdateTime()
                && newValue.getLastFileUpdateTime() < System.currentTimeMillis();
        if (newerAndValid) {
            newValue.setLastReceiveTime(System.currentTimeMillis());
            keywordData.put(keywordHash, newValue);
            return true;
        }
        return false;
    }

    /**
     * 获取需要重新发布的文件记录。
     * 最近一段时间之内接收过重新发布请求的则不需要再重新发布
     */
    public Map<HashCode160, KeywordValue> getDataForRepublish() {
        Map<HashCode160, KeywordValue> result = new HashMap<>(16);
        for(Map.Entry<HashCode160, KeywordValue> entry : keywordData.entrySet()) {
            KeywordValue keywordValue = entry.getValue();
            if(TimeUtils.msAgo(leastRepublishMs, keywordValue.getLastReceiveTime())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 获取新节点加入时需要存储到其上的kv数据
     */
    public Map<HashCode160, KeywordValue> getDataForNewNode(HashCode160 newNodeId) {
        HashCode160 localNodeId = localNodeService.getLocalNodeId();
        Map<HashCode160, KeywordValue> result = new HashMap<>(16);
        for (Map.Entry<HashCode160, KeywordValue> entry : keywordData.entrySet()) {
            KeywordValue value = entry.getValue();
            if(entry.getKey().calculateDistance(localNodeId) > entry.getKey().calculateDistance(newNodeId)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 清除过期（基于发布时间）的关键词对应的文件，该方法由定时器按照一定时间调用即可。
     */
    public void cleanTimeoutRecords() {
        for(KeywordValue keywordValue : keywordData.values()) {
            Map<HashCode160, File> id2File = keywordValue.getId2File();
            for(Map.Entry<HashCode160, File> entry : id2File.entrySet()) {
                if (TimeUtils.msAgo(0, entry.getValue().getExpireTime())) {
                    id2File.remove(entry.getKey());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean loadFromBackup() {
        try {
            keywordData = localStorage.load(LocalStorageTypeEnum.DHT_KEYWORD_PAIRS.getTypeName(), null, Map.class);
            return true;
        } catch (Exception e) {
            LOGGER.error("Read keyword data from local storage raised an error.", e);
            return false;
        }
    }


    public boolean backup() {
        try {
            localStorage.save(LocalStorageTypeEnum.DHT_KEYWORD_PAIRS.getTypeName(), null, keywordData);
            return true;
        } catch (Exception e) {
            LOGGER.error("Backup keyword data to local storage raised an error.", e);
            return false;
        }
    }
}
