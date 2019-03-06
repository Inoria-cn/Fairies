package org.squirrelnest.fairies.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Inoria on 2019/3/5.
 */
public class BinaryUtils {

    private static Character[] getBinaryCharSequence(byte aByte) {
        Character[] binaryChars = new Character[8];
        for(int i = 0; i < 8; i++) {
            byte leftPart = (byte)(aByte >> i);
            char digit = Character.forDigit(leftPart % 2, 0);
            binaryChars[7-i] = digit;
        }
        return binaryChars;
    }

    public static Character[] transfer(byte ...bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return new Character[0];
        }
        List<Character> list = new ArrayList<Character>(bytes.length * 8);
        for(int i = 0, length = bytes.length; i < length; i++) {
            Character[] transferAByte = getBinaryCharSequence(bytes[i]);
            list.addAll(Arrays.asList(transferAByte));
        }
        return list.toArray(new Character[list.size()]);
    }

    public static int getBinaryDigit(byte aByte, int index) {
        char binaryChar = getBinaryCharSequence(aByte)[index];
        return Integer.parseInt(binaryChar + "");
    }
}
