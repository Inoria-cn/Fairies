package org.squirrelnest.fairies.network;

import org.apache.http.conn.HttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * 用于定时清除无效httpClient连接，也可以使用spring定时器实现
 * Created by Inoria on 2019/3/17.
 */
@Component
public class IdleConnectionCleaner extends Thread {

    private boolean running = true;
    //定时清除无效连接的毫秒数
    private final long interval = 5000L;

    private static final Logger LOGGER = LoggerFactory.getLogger(IdleConnectionCleaner.class);

    @Resource
    private HttpClientConnectionManager httpClientConnectionManager;

    @PostConstruct
    private void init() {
        super.start();
    }

    @Override
    public void run() {
        try {
            while (running) {
                httpClientConnectionManager.closeExpiredConnections();
                wait(interval);
            }
        } catch (Exception e) {
            LOGGER.error("Wait method throw an error.", e);
        }
    }

    @PreDestroy
    private void showDown() {
        running = false;
        httpClientConnectionManager.shutdown();
    }
}
