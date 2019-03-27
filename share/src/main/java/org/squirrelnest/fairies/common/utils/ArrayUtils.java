package org.squirrelnest.fairies.common.utils;

/**
 * Created by Inoria on 2019/3/25.
 */
public class ArrayUtils {

    public static void batchAppend(byte[] container, byte[] data, int offset) {
        for(int i = 0, length = data.length; i < length; i++) {
            container[offset + i] = data[i];
        }
    }

    public static byte[] buildZeroByteArray(int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = 0;
        }
        return result;
    }
}
