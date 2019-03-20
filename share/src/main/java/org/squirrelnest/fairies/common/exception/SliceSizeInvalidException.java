package org.squirrelnest.fairies.common.exception;

/**
 * Created by Inoria on 2019/3/20.
 */
public class SliceSizeInvalidException extends RuntimeException {

    public SliceSizeInvalidException() {
        super("Slice size is not 16kb * 2^n, invalid size.");
    }
}
