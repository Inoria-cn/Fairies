package org.squirrelnest.fairies.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Inoria on 2019/3/18.
 */
public class MapBuilder<K, V> {

    private Map<K, V> targetMap = new HashMap<K, V>(16);

    public MapBuilder<K, V> addField(K key, V value) {
        targetMap.put(key, value);
        return this;
    }

    public MapBuilder<K, V> addFields(Map<K, V> fields) {
        targetMap.putAll(fields);
        return this;
    }

    public Map<K, V> build() {
        return this.targetMap;
    }
}
