package org.squirrelnest.fairies.thread.inform;

import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

/**
 * Created by Inoria on 2019/3/16.
 */
public class ResponseCountInform implements Inform<Integer> {

    private Integer state = 0;

    @Override
    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public Integer getState() {
        return this.state;
    }

    @Override
    public void blockUntilState(Integer targetState, Long maxMs) {
        Long startMs = System.currentTimeMillis();
        while (true) {
            Long currentMs = System.currentTimeMillis();
            if (currentMs - startMs >= maxMs || state >= targetState) {
                break;
            }

            try {
                Thread.sleep(100);
            } catch (Exception e) {
                //do nothing
            }
        }
    }

    @Override
    public synchronized void inform() {
        this.state++;
    }
}
