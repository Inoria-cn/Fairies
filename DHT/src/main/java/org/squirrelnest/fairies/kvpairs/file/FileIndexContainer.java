package org.squirrelnest.fairies.kvpairs.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
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
public class FileIndexContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileIndexContainer.class);

    @Value("${fairies.dht.republish.cancelMs}")
    private Long leastRepublishMs;

    @Value("${fairies.dht.timeout}")
    private Long expireMs;

    @Resource(name = "localStorageDAO")
    private DataSource localStorage;

    @Resource
    private LocalNodeService localNodeService;

    /**
     * 系统启动时读取本地存储，同时使用定时器定期备份该内存数据到本地文件中。
     */
    private Map<HashCode160, FileValue> fileData;

    @PostConstruct
    private void init() {
        boolean loadResult = loadFromBackup();
        if (!loadResult || fileData == null) {
            fileData = new HashMap<>(16);
        }
    }

    @PreDestroy
    private void gentleDestroy() {
        backup();
    }

    public FileValue get(HashCode160 targetKey) {
        return fileData.get(targetKey);
    }

    public void put(HashCode160 key, FileValue value) {
        fileData.put(key, value);
    }

    public Map<HashCode160, FileValue> getAllData() {
        return fileData;
    }

    /**
     * 获取需要重新发布的文件记录。
     * 最近一段时间之内有更新的则不需要重新发布
     */
    public Map<HashCode160, FileValue> getDataForRepublish() {
        Map<HashCode160, FileValue> result = new HashMap<>(16);
        for(Map.Entry<HashCode160, FileValue> entry : fileData.entrySet()) {
            FileValue fileValue = entry.getValue();
            if(TimeUtils.msAgo(leastRepublishMs, fileValue.getLastUpdateTime())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 获取新节点加入时需要存储到其上的kv数据
     */
    public Map<HashCode160, FileValue> getDataForNewNode(HashCode160 newNodeId) {
        HashCode160 localNodeId = localNodeService.getLocalNodeId();
        Map<HashCode160, FileValue> result = new HashMap<>(16);
        for (Map.Entry<HashCode160, FileValue> entry : fileData.entrySet()) {
            FileValue value = entry.getValue();
            if(entry.getKey().calculateDistance(localNodeId) > entry.getKey().calculateDistance(newNodeId)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * 清除过期（基于发布时间）的kv对，该方法由定时器按照一定时间调用即可。
     */
    public void cleanTimeoutRecords() {
        for(Map.Entry<HashCode160, FileValue> entry : fileData.entrySet()) {
            FileValue fileValue = entry.getValue();
            if(TimeUtils.msAgo(0, fileValue.getExpireTimestamp())) {
                fileData.remove(entry.getKey());
            }
        }
    }

    /**
     * 备份文件kv数据到本地存储中
     */
    public boolean backup() {
        try {
            localStorage.save(LocalStorageTypeEnum.DHT_FILE_LOCATION_PAIRS.getTypeName(), null, fileData);
            return true;
        } catch (Exception e) {
            LOGGER.error("Backup to local storage failed.", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean loadFromBackup() {
        try {
            fileData = localStorage.load(LocalStorageTypeEnum.DHT_FILE_LOCATION_PAIRS.getTypeName(), null, Map.class);
            return true;
        } catch (Exception e) {
            LOGGER.error("Read file data from local storage raised an error.", e);
            return false;
        }
    }
}
