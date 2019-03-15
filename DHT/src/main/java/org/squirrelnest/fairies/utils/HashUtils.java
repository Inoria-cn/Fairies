package org.squirrelnest.fairies.utils;

import org.squirrelnest.fairies.domain.HashCode160;

/**
 * Created by Inoria on 2019/3/5.
 */
public class HashUtils {

    public static HashCode160 generateLocalHash() {
        String classPath = System.getProperty("java.class.path");
        double randomNumber = Math.random();
        Long currentTime = System.currentTimeMillis();
        return HashCode160.newInstance(classPath + randomNumber + currentTime);
    }
}
