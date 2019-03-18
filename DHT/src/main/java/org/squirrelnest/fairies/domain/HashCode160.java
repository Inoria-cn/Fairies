package org.squirrelnest.fairies.domain;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.squirrelnest.fairies.utils.BinaryUtils;

import java.util.Arrays;
import java.util.Base64;

/**
 * DHT系统的基础模型：160hash值作为id
 * Created by Inoria on 2019/3/5.
 */
public class HashCode160 {

    //使用20个元素的字节数组来表示160个比特
    private byte[] bits;

    private void init() {
        bits = new byte[20];
        for(int i = 0; i < 20; i++) {
            bits[i] = 0;
        }
    }

    public HashCode160() {
        init();
    }

    public HashCode160(byte[] bytes160l) {
        if (bytes160l == null || bytes160l.length != 20) {
            init();
            return;
        }

        this.bits = bytes160l.clone();
    }

    public static HashCode160 newInstance(String rawText) {
        HashCode160 hashCode160 = new HashCode160();
        hashCode160.setBits(DigestUtils.sha1(rawText));
        return hashCode160;
    }

    public byte[] getBits() {
        return bits;
    }

    public void setBits(byte[] bits) {
        this.bits = bits;
    }

    public Character[] bitsAsChar() {
        return BinaryUtils.transfer(bits);
    }

    public Integer calculateDistance(HashCode160 that) {
        byte[] myBits = this.getBits();
        byte[] yourBits = that.getBits();
        int firstDifferentPosition = 0;
        while (firstDifferentPosition < 20) {
            if (myBits[firstDifferentPosition] != yourBits[firstDifferentPosition]) {
                break;
            }
            firstDifferentPosition++;
        }
        if (firstDifferentPosition == 20) {
            return 0;
        }
        int samePrefixLength = BinaryUtils.findMaxSamePrefixLength(myBits[firstDifferentPosition], yourBits[firstDifferentPosition]);
        return  160 - firstDifferentPosition * 8 - samePrefixLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HashCode160)) return false;

        HashCode160 that = (HashCode160) o;

        return Arrays.equals(getBits(), that.getBits());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBits());
    }

    /**
     * 获取的是按byte进行转换的字符串，用于序列化传输
     * @return 序列化后的字符串
     */
    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(getBits());
    }

    public static HashCode160 parseString(String raw) {
        if (StringUtils.isEmpty(raw)) {
            return null;
        }
        return new HashCode160(Base64.getDecoder().decode(raw));
    }
}
