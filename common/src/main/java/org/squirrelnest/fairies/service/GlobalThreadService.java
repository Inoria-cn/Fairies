package org.squirrelnest.fairies.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 程序共享的一个线程池，用于方便地开启一段多线程
 * Created by Inoria on 2019/3/18.
 */
@Service
public class GlobalThreadService {

    @Value("${fairies.dht.alpha}")
    private int alpha;

    private ExecutorService pool;

    @PostConstruct
    private void init() {
        pool = new ThreadPoolExecutor(this.alpha * 4, this.alpha * 8,
                3, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    public void makeItRun(Runnable target) {
        pool.execute(target);
    }

    @PreDestroy
    private void gentleShutdown() {
        pool.shutdownNow();
    }
}
