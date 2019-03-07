package org.squirrelnest.fairies.exception;

/**
 * Created by Inoria on 2019/3/7.
 */
public class GetNodeException extends RuntimeException {

    public GetNodeException() {
        super();
    }

    public GetNodeException(String message) {
        super(message);
    }

    public GetNodeException(String message, Throwable t) {
        super(message, t);
    }
}
