package org.squirrelnest.fairies.thread.inform;

import org.squirrelnest.fairies.thread.inform.interfaces.Inform;

/**
 * Created by Inoria on 2019/3/16.
 */
public class MessageGetInform implements Inform<Boolean> {

    private Boolean state = false;

    @Override
    public void setState(Boolean state) {
        this.state = state;
    }

    @Override
    public void blockUntilState(Boolean targetState, Long maxMs) {
        Long startMs = System.currentTimeMillis();
        while (true) {
            Long currentMs = System.currentTimeMillis();
            if (currentMs - startMs >= maxMs || state == targetState) {
                break;
            }
        }
    }

    @Override
    public void inform() {
        this.state = true;
    }
}
