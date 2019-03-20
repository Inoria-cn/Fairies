package org.squirrelnest.fairies.common.utils;

/**
 * Created by Inoria on 2019/3/20.
 */
public class MathUtils {

    public static boolean is2PowerN(Object number) {
        if (number instanceof Double || number instanceof Float) {
            return false;
        }
        Long value = (Long)number;
        while(value > 0) {
            if(value % 2 != 0) {
                return false;
            }
            value /= 2;
        }
        return true;
    }
}
