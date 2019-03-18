package org.squirrelnest.fairies.domain;

import com.alibaba.fastjson.JSON;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.squirrelnest.fairies.TestUtils.o;

/**
 * Created by Inoria on 2019/3/18.
 */
public class HashCode160Test {

    @Test
    public void testSerialize() {
        HashCode160 hashCode160 = HashCode160.newInstance("test");
        String toString = hashCode160.toString();
        HashCode160 fromStaticMethod = HashCode160.parseString(toString);
        String serialized = JSON.toJSONString(hashCode160);
        HashCode160 deSerialized = JSON.parseObject(serialized, HashCode160.class);
        o(toString);
        o(fromStaticMethod);
        o(serialized);
        o(deSerialized);
        o(HashCode160.parseString("qUqP5cyxm6YcTAhz05Hph5gvu9M="));
    }
}