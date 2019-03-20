package org.squirrelnest.fairies.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.squirrelnest.fairies.common.domain.FileMetadata;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.storage.datasource.interfaces.DataSource;
import org.squirrelnest.fairies.storage.enumeration.LocalStorageTypeEnum;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/20.
 */
@Component
public class LocalFileMetadataContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileMetadataContainer.class);

    @Resource(name = "localStorageDAO")
    private DataSource localStorage;

    private Map<HashCode160, FileMetadata> data;

    @PostConstruct
    private void init() {
        boolean loadResult = loadFromBackup();
        if (!loadResult || data == null) {
            data = new HashMap<>(16);
        }
    }

    @PreDestroy
    private void gentleShowdown() {
        backup();
    }



    /**
     * 备份文件kv数据到本地存储中
     */
    public boolean backup() {
        try {
            localStorage.save(LocalStorageTypeEnum.LOCAL_SHARE_FILE.getTypeName(), null, data);
            return true;
        } catch (Exception e) {
            LOGGER.error("Backup to meta storage failed.", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean loadFromBackup() {
        try {
            data = localStorage.load(LocalStorageTypeEnum.LOCAL_SHARE_FILE.getTypeName(), null, Map.class);
            return true;
        } catch (Exception e) {
            LOGGER.error("Read file data from meta storage raised an error.", e);
            return false;
        }
    }
}
