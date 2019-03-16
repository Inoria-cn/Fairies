package org.squirrelnest.fairies.utils;

import java.util.List;

/**
 * Created by Inoria on 2019/3/16.
 */
public class CollectionUtils {

    public static <T> void replace(List<T> collection, int index, T replace) {
        if (collection == null || collection.size() < index + 1) {
            throw new RuntimeException("Index out of bound.");
        }
        collection.remove(index);
        collection.add(index, replace);
    }
}
