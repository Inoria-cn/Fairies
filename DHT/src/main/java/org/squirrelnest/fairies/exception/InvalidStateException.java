package org.squirrelnest.fairies.exception;

/**
 * Created by Inoria on 2019/3/15.
 */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException() {
        super("This state should never occur in system, check logic.");
    }
}
