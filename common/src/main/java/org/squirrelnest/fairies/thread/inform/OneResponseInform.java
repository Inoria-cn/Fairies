package org.squirrelnest.fairies.thread.inform;

import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

import java.util.concurrent.ExecutorService;

/**
 * Created by Inoria on 2019/3/16.
 */
public class OneResponseInform implements Inform<Boolean> {

    private Boolean state = false;

    @Override
    public void setState(Boolean state) {
        this.state = state;
    }

    @Override
    public Boolean getState() {
        return this.state;
    }

    @Override
    public void blockUntilState(Boolean targetState, Long maxMs) {
        Long startMs = System.currentTimeMillis();
        while (true) {
            Long currentMs = System.currentTimeMillis();
            if (currentMs - startMs >= maxMs || state == targetState) {
                break;
            }

            try {
                Thread.sleep(10);
            } catch (Exception e) {
                //do nothing
            }
        }
    }

    @Override
    public synchronized void inform() {
        this.state = true;
    }
}
