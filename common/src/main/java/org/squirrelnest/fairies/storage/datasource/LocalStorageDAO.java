package org.squirrelnest.fairies.storage.datasource;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.squirrelnest.fairies.storage.datasource.interfaces.DataSource;
import org.squirrelnest.fairies.storage.enumeration.LocalStorageTypeEnum;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/6.
 *
 * 用于存储内存中的数据到文件中，节点程序如果停止运行，重启后可以获得部分数据
 */
@Repository
public class LocalStorageDAO implements DataSource {
    @Value("${fairies.localstorage.basepath}")
    private String basePath;

    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalStorageDAO.class);

    @Override
    public <T> void save(String partName, String key, T value) throws Exception {
        LocalStorageTypeEnum typeEnum = LocalStorageTypeEnum.findType(partName);
        if (typeEnum.equals(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE)) {
            saveKVPair(key, value);
        }
        backup(typeEnum, value);
    }

    @Override
    public <T> T load(String partName, String key, Class<T> valueClass) throws Exception {
        LocalStorageTypeEnum typeEnum = LocalStorageTypeEnum.findType(partName);
        if (typeEnum.equals(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE)) {
            return loadKVPair(key, valueClass);
        }
        return loadBackup(typeEnum, valueClass);
    }

    private File getBackupFile(LocalStorageTypeEnum typeEnum) {
        String fileFullPath = basePath + typeEnum.getFileName();
        return new File(fileFullPath);
    }

    private void createNewFileIfNotExists(LocalStorageTypeEnum typeEnum, File file) throws Exception {
        if (!file.exists()) {
            boolean success = file.createNewFile();
            if (!success) {
                String errorMessage = "meta storage backup file create failed, related file name is " + typeEnum.getFileName();
                LOGGER.error(errorMessage);
                throw new Exception(errorMessage);
            }
        }
    }

    private <T> void backup(LocalStorageTypeEnum typeEnum, T value) throws Exception {
        File file = getBackupFile(typeEnum);
        createNewFileIfNotExists(typeEnum, file);

        FileUtils.writeStringToFile(file, JSON.toJSONString(value), DEFAULT_ENCODING);
    }

    private <T> T loadBackup(LocalStorageTypeEnum typeEnum, Class<T> valueClass) throws Exception {
        File file = getBackupFile(typeEnum);
        if (!file.exists()) {
            String errorMessage = "meta storage backup file can not find, related file name is " + typeEnum.getFileName();
            LOGGER.error(errorMessage);
            return null;
        }
        String fileContent = FileUtils.readFileToString(file, DEFAULT_ENCODING);
        return JSON.parseObject(fileContent, valueClass);
    }

    @SuppressWarnings("unchecked")
    private <T> void saveKVPair(String key, T value) throws Exception {
        File file = getBackupFile(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE);
        createNewFileIfNotExists(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE, file);
        Map<String, Object> storeKvMap;
        try {
            String fileString = FileUtils.readFileToString(file, DEFAULT_ENCODING);
            storeKvMap = JSON.parseObject(fileString, HashMap.class);
        } catch (Exception e) {
            storeKvMap = new HashMap<>(16);
        }
        storeKvMap.put(key, value);
        FileUtils.writeStringToFile(file, JSON.toJSONString(storeKvMap), DEFAULT_ENCODING);
    }

    @SuppressWarnings("unchecked")
    private <T> T loadKVPair(String key, Class<T> valueClass) throws Exception {
        File file = getBackupFile(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE);
        if (!file.exists()) {
            return null;
        }
        Map<String, Object> storeKvMap;
        try {
            String fileString = FileUtils.readFileToString(file, DEFAULT_ENCODING);
            storeKvMap = JSON.parseObject(fileString, HashMap.class);
        } catch (Exception e) {
            return null;
    }
        return JSON.parseObject(storeKvMap.get(key).toString(), valueClass);
    }
}
