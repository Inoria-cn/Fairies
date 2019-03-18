package org.squirrelnest.fairies;

import com.alibaba.fastjson.JSON;

/**
 * Created by Inoria on 2019/3/18.
 */
public class TestUtils {

    public static void o(Object object) {
        System.out.println(JSON.toJSONString(object));
    }
}
