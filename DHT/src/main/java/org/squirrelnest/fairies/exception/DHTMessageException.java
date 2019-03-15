package org.squirrelnest.fairies.exception;

/**
 * DHT协议消息传输异常
 * Created by Inoria on 2019/3/13.
 */
public class DHTMessageException extends RuntimeException {
    public DHTMessageException() {
        super();
    }

    public DHTMessageException(String message) {
        super(message);
    }

    public DHTMessageException(String message, Throwable t) {
        super(message, t);
    }
}
