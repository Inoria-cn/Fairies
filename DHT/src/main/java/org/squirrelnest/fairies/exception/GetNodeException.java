package org.squirrelnest.fairies.exception;

/**
 * 查找节点时出现的异常
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
