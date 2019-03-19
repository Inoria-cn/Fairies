package org.squirrelnest.fairies.kvpairs;

/**
 * Created by Inoria on 2019/3/17.
 */
public enum KVValueTypeEnum {
    FILE("file"),
    KEYWORD("keyword"),
    /**
     * 用于为某个keyword索引项添加一个新文件
     */
    KEYWORD_FILE("keyword_file"),
    UNKNOWN_TYPE("unknown");

    private String value;

    KVValueTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static KVValueTypeEnum find(String value) {
        for(KVValueTypeEnum typeEnum : KVValueTypeEnum.values()) {
            if (typeEnum.getValue().equalsIgnoreCase(value)) {
                return typeEnum;
            }
        }
        return UNKNOWN_TYPE;
    }
}
