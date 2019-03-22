package org.squirrelnest.fairies.schedule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.squirrelnest.fairies.domain.HashCode160;
import org.squirrelnest.fairies.domain.Record;
import org.squirrelnest.fairies.kvpairs.KVValueTypeEnum;
import org.squirrelnest.fairies.kvpairs.file.FileIndexContainer;
import org.squirrelnest.fairies.kvpairs.file.model.FileValue;
import org.squirrelnest.fairies.kvpairs.keyword.KeywordIndexContainer;
import org.squirrelnest.fairies.kvpairs.keyword.model.KeywordValue;
import org.squirrelnest.fairies.procedure.FindValue;
import org.squirrelnest.fairies.procedure.Store;
import org.squirrelnest.fairies.router.RouterTable;
import org.squirrelnest.fairies.service.RequestSendService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Inoria on 2019/3/19.
 */
public class RepublishScheduleRunner {

    @Value("${fairies.dht.k}")
    private Integer k;

    @Value("${fairies.dht.alpha}")
    private Integer alpha;

    @Value("${fairies.dht.request.timeout}")
    private Integer timeout;

    @Resource
    private KeywordIndexContainer keywordIndexContainer;

    @Resource
    private FileIndexContainer fileIndexContainer;

    @Resource
    private RequestSendService requestSendService;

    @Resource
    private RouterTable routerTable;

    private ExecutorService pool;

    @PostConstruct
    private void init() {
        pool = new ThreadPoolExecutor(this.alpha, this.alpha * 8,
                6, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @PreDestroy
    private void shutdown() {
        pool.shutdownNow();
    }

    @Scheduled(initialDelay = 1800000, fixedRate = 3600000L)
    public void republishForFileKV() {
        Map<HashCode160, FileValue> data = fileIndexContainer.getDataForRepublish();
        for (Map.Entry<HashCode160, FileValue> entry : data.entrySet()) {
            pool.execute(() -> {
                FindValue findValue = new FindValue(entry.getKey(), k, alpha, timeout, KVValueTypeEnum.FILE,
                        routerTable, requestSendService);
                List<Record> nearNodes = findValue.execute().getNearerNodes();
                Store store = new Store(k, alpha, timeout, KVValueTypeEnum.FILE, nearNodes,
                        null, entry.getValue(), entry.getKey(), null, routerTable, requestSendService);
                store.execute();
            });
        }
    }

    @Scheduled(initialDelay = 2000000, fixedRate = 3600000L)
    public void republishForKeywordKV() {
        Map<HashCode160, KeywordValue> data = keywordIndexContainer.getDataForRepublish();
        for (Map.Entry<HashCode160, KeywordValue> entry : data.entrySet()) {
            pool.execute(() -> {
                FindValue findValue = new FindValue(entry.getKey(), k, alpha, timeout, KVValueTypeEnum.KEYWORD,
                        routerTable, requestSendService);
                List<Record> nearNodes = findValue.execute().getNearerNodes();
                Store store = new Store(k, alpha, timeout, KVValueTypeEnum.KEYWORD, nearNodes,
                        null, entry.getValue(), entry.getKey(), null, routerTable, requestSendService);
                store.execute();
            });
        }
    }
}
