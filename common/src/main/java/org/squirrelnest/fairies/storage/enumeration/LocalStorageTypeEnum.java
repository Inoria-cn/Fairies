package org.squirrelnest.fairies.storage.enumeration;

/**
 * Created by Inoria on 2019/3/6.
 */
public enum LocalStorageTypeEnum {

    /**
     * DHT网络上，该节点所负责的关键词索引信息
     */
    DHT_KEYWORD_PAIRS("keywordPairs", "DHTkeywordPairs.fairies"),

    /**
     * DHT网络上，该节点所负责的文件分享索引tracker信息
     */
    DHT_FILE_LOCATION_PAIRS("fileLocationPairs", "DHTFileLocationPairs.fairies"),

    /**
     * DHT网络路由表
     */
    DHT_ROUTER_TABLE("routerTable", "DHTRouterTable.fairies"),

    /**
     * 本机发布的文件的信息
     */
    LOCAL_SHARE_FILE("localShareFile", "localShareFile.fairies"),

    /**
     * 其他未分类本机存储
     */
    OTHER_LOCAL_STORAGE("otherData", "otherData.fairies");


    private String typeName;
    private String fileName;

    LocalStorageTypeEnum(String typeName, String fileName) {
        this.typeName = typeName;
        this.fileName = fileName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getFileName() {
        return fileName;
    }

    public static LocalStorageTypeEnum findType(String typeName) {
        for(LocalStorageTypeEnum typeEnum : LocalStorageTypeEnum.values()) {
            if (typeEnum.typeName.equals(typeName)) {
                return typeEnum;
            }
        }
        return OTHER_LOCAL_STORAGE;
    }
}
