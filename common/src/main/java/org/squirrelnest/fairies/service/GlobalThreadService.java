package org.squirrelnest.fairies.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
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

    @Resource
    private ConfigReadService configReadService;

    private ExecutorService pool;

    @PostConstruct
    private void init() {
        int alpha = configReadService.getDHTParamAlpha();
        pool = new ThreadPoolExecutor(alpha * 4, alpha * 8,
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
