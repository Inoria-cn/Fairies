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

    public static byte setDigit(byte origin, int index, boolean newBit) {

        boolean oldBit = getBinaryDigit(origin, index) > 0;
        int bitIntValue = Math.round((float)Math.pow(2, index));
        int newValue = origin & 0xff;
        if(oldBit && !newBit) {
            newValue -= bitIntValue;
        } else if(!oldBit && newBit) {
            newValue += bitIntValue;
        }
        return (byte)(newValue & 0xff);
    }

    public static int findMaxSamePrefixLength(byte a, byte b) {
        int length = 8;
        int aValue = a, bValue = b;
        while(length > 0) {
            if (aValue == bValue) {
                return length;
            }
            aValue = aValue >> 1;
            bValue = bValue >> 1;
            length--;
        }
        return 0;
    }

    public static byte[] getByteArrayFromBooleanArray(boolean[] source) {
        if (source == null || source.length == 0) {
            return new byte[0];
        }
        byte[] result = new byte[(source.length - 1) / 8 + 1];
        int byteValueSum = 0;
        for(int i = 0; i < source.length; i++) {
            int indexInByte = i % 8;
            if(source[i]) {
                byteValueSum += Math.pow(2, indexInByte);
            }
            if(indexInByte == 7 || i == source.length - 1) {
                int indexInArray = i / 8;
                result[indexInArray] = (byte)(byteValueSum & 0xff);
                byteValueSum = 0;
            }
        }
        return result;
    }
}
