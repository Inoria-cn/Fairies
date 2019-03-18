package org.squirrelnest.fairies.schedule;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.squirrelnest.fairies.kvpairs.file.FileIndexContainer;
import org.squirrelnest.fairies.kvpairs.keyword.KeywordIndexContainer;
import org.squirrelnest.fairies.router.RouterTable;

import javax.annotation.Resource;

/**
 * Created by Inoria on 2019/3/14.
 */
@Component
public class ScheduleRunner {

    @Resource
    private KeywordIndexContainer keywordIndexContainer;

    @Resource
    private FileIndexContainer fileIndexContainer;

    @Resource
    private RouterTable routerTable;

    @Scheduled(initialDelay = 600000, fixedRate = 3600000L)
    public void timedTaskHourForFile() {
        fileIndexContainer.cleanTimeoutRecords();
        fileIndexContainer.backup();
    }

    @Scheduled(initialDelay = 600000, fixedRate = 3600000L)
    public void timedTaskHourForRouterTable() {
        routerTable.refreshOldBuckets();
    }

    @Scheduled(initialDelay = 900000, fixedRate = 3600000L)
    public void timedTaskHourForKeyword() {
        keywordIndexContainer.cleanTimeoutRecords();
        keywordIndexContainer.backup();
    }

}
