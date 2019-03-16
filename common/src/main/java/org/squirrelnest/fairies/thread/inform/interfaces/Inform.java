package org.squirrelnest.fairies.thread.inform.interfaces;

/**
 * Created by Inoria on 2019/3/16.
 */
public interface Inform<T> {
    /**
     * 调用该方法重置状态标志
     * @param state 新的状态
     */
    void setState(T state);

    void blockUntilState(T targetState, Long maxMilliseconds);

    void inform();
}
