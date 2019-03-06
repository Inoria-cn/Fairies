package org.squirrelnest.fairies.storage.datasource;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.storage.datasource.interfaces.KVDataSource;
import org.squirrelnest.fairies.storage.enumeration.LocalStorageTypeEnum;

import java.io.File;

/**
 * Created by Inoria on 2019/3/6.
 *
 * 用于存储内存中的数据到文件中，节点程序如果停止运行，重启后可以获得部分数据
 */
@Service
public class LocalStorageService implements KVDataSource {
    @Value("${fairies.localstorage.basepath}")
    private String basePath;

    public static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageService.class);

    @Override
    public <T> void save(String partName, String key, T value) throws Exception {
        LocalStorageTypeEnum typeEnum = LocalStorageTypeEnum.findType(partName);
        if (typeEnum.equals(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE)) {
            String errorMessage = "Target part name is not supported in local storage service";
            LOGGER.error(errorMessage);
            throw new UnsupportedOperationException(errorMessage);
        }
        backup(typeEnum, value);
    }

    @Override
    public <T> T load(String partName, String key, Class<T> valueClass) throws Exception {
        LocalStorageTypeEnum typeEnum = LocalStorageTypeEnum.findType(partName);
        if (typeEnum.equals(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE)) {
            String errorMessage = "Target part name is not supported in local storage service";
            LOGGER.error(errorMessage);
            throw new UnsupportedOperationException(errorMessage);
        }
        return loadBackup(typeEnum, valueClass);
    }

    private File getBackupFile(LocalStorageTypeEnum typeEnum) {
        String fileFullPath = basePath + typeEnum.getFileName();
        return new File(fileFullPath);
    }

    private <T> void backup(LocalStorageTypeEnum typeEnum, T value) throws Exception {
        File file = getBackupFile(typeEnum);
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                String errorMessage = "local storage backup file create failed, related file name is " + typeEnum.getFileName();
                LOGGER.error(errorMessage);
                throw new Exception(errorMessage);
            }
        }

        FileUtils.writeStringToFile(file, JSON.toJSONString(value), "UTF-8");
    }

    private <T> T loadBackup(LocalStorageTypeEnum typeEnum, Class<T> valueClass) throws Exception {
        File file = getBackupFile(typeEnum);
        if (!file.exists()) {
            String errorMessage = "local storage backup file can not find, related file name is " + typeEnum.getFileName();
            LOGGER.error(errorMessage);
            return null;
        }
        String fileContent = FileUtils.readFileToString(file, "UTF-8");
        return JSON.parseObject(fileContent, valueClass);
    }
}
