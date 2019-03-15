package org.squirrelnest.fairies.utils;

/**
 * Created by Inoria on 2019/3/14.
 */
public class TimeUtils {

    public static boolean msAgo(long ms, long forCompare) {
        long currentMs = System.currentTimeMillis();
        return forCompare + ms < currentMs;
    }
}
