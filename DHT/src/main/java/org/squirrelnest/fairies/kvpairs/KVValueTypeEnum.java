package org.squirrelnest.fairies.kvpairs;

/**
 * Created by Inoria on 2019/3/17.
 */
public enum KVValueTypeEnum {
    FILE("file"),
    KEYWORD("keyword"),
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
