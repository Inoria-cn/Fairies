package org.squirrelnest.fairies.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.squirrelnest.fairies.model.HashCode160;

/**
 * Created by Inoria on 2019/3/5.
 */
public class HashUtils {

    public static HashCode160 getLocalHash() {
        DigestUtils.sha1();
    }
}
