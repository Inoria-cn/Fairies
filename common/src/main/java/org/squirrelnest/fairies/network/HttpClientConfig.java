package org.squirrelnest.fairies.network;

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Inoria on 2019/3/17.
 */
@Configuration
public class HttpClientConfig {

    //最大连接数
    private final Integer maxTotal = 100;
    //并发数
    private final Integer defaultMaxPerRoute = 16;

    @Bean
    public PoolingHttpClientConnectionManager httpClientConnectionManager() {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        manager.setMaxTotal(maxTotal);
        return manager;
    }

    @Bean
    public HttpClientBuilder httpClientBuilder(@Qualifier("httpClientConnectionManager") PoolingHttpClientConnectionManager manager) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setConnectionManager(manager);
        return builder;
    }
}
