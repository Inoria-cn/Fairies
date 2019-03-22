package org.squirrelnest.fairies.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.storage.datasource.interfaces.DataSource;
import org.squirrelnest.fairies.storage.enumeration.LocalStorageTypeEnum;
import org.squirrelnest.fairies.utils.HashUtils;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/7.
 */
@Service
public class LocalNodeService {

    private static final String NODE_ID_KEY = "localNodeId";
    private static final String STARTER_KEY = "starterNodeAddress";

    private static final String PARAM_KEY_ID = "nodeId";
    private static final String PARAM_KEY_IP = "nodeIp";
    private static final String PARAM_KEY_PORT = "nodePort";

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalNodeService.class);

    @Value("${server.port}")
    private String localPort;

    @Value("${fairies.dht.presetNode}")
    private String presetAddress;

    @Resource(name = "localStorageDAO")
    private DataSource localStore;

    public HashCode160 getLocalNodeId() {
        HashCode160 localId = null;
        try {
            localId = localStore.load(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE.getTypeName(), NODE_ID_KEY, HashCode160.class);
        } catch (Exception e) {
            LOGGER.error("Load meta storage failed, nested meta key is " + NODE_ID_KEY, e);
        }
        if (localId == null) {
            localId = HashUtils.generateLocalHash();
            try {
                localStore.save(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE.getTypeName(), NODE_ID_KEY, localId);
            } catch (Exception e) {
                LOGGER.error("save meta storage failed, nested meta key is " + NODE_ID_KEY, e);
            }
        }
        return localId;
    }

    public Record getLocalNodeRecord() {
        Record record = new Record();
        record.setNodeId(getLocalNodeId());
        record.setNodeIp(getLocalIp());
        record.setNodePort(localPort);
        return record;
    }

    public Map<String, String> getLocalAddressParams() {
        Map<String, String> result = new HashMap<>(8);
        result.put(PARAM_KEY_ID, getLocalNodeId().toString());
        result.put(PARAM_KEY_IP, getLocalIp());
        result.put(PARAM_KEY_PORT, localPort);
        return result;
    }

    private String getLocalIp() {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return address.getHostAddress();
        } catch (Exception e) {
            LOGGER.error("Cannot get meta host ip!", e);
            return null;
        }
    }

    public Record getStarterNode() {
        String starterAddress = null;
        try {
            starterAddress = localStore.load(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE.getTypeName(),
                    STARTER_KEY, String.class);
        } catch (Exception e) {
            LOGGER.error("Load meta storage failed, nested meta key is " + STARTER_KEY, e);
        }
        if (starterAddress == null) {
            starterAddress = presetAddress;
        }
        Record record = new Record();
        record.setNodeIp(starterAddress.split(":")[0]);
        record.setNodePort(starterAddress.split(":")[1]);
        return record;
    }

    public boolean setStarterNode(String ip, String port) {
        try {
            localStore.save(LocalStorageTypeEnum.OTHER_LOCAL_STORAGE.getTypeName(), STARTER_KEY,
                    ip + ":" + port);
            return true;
        } catch (Exception e) {
            LOGGER.error("Save meta storage failed, nested meta key is " + STARTER_KEY, e);
            return false;
        }
    }
}
