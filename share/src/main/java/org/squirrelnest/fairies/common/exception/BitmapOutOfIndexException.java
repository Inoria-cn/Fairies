package org.squirrelnest.fairies.common.exception;

/**
 * Created by Inoria on 2019/3/20.
 */
public class BitmapOutOfIndexException extends ArrayIndexOutOfBoundsException {
    public BitmapOutOfIndexException() {
        super("The index is out of bound when trying to manipulate SliceBitmap");
    }

    public BitmapOutOfIndexException(String msg) {
        super(msg);
    }
}
