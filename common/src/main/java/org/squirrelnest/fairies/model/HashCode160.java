package org.squirrelnest.fairies.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.squirrelnest.fairies.utils.BinaryUtils;

/**
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

    public Character[] getBitsAsChar() {
        return BinaryUtils.transfer(bits);
    }
}
