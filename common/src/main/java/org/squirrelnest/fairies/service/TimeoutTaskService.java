package org.squirrelnest.fairies.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * 超时任务线程池
 * Created by Inoria on 2019/3/24.
 */
@Service
public class TimeoutTaskService {

    public class TaskRunState {
        private Boolean taskStartRun;
        private Boolean taskRunSuccess;
        private Long taskRunMillis;

        public TaskRunState() {
            this.taskStartRun = false;
            this.taskRunSuccess = false;
        }

        public Boolean getTaskStartRun() {
            return taskStartRun;
        }

        public void setTaskStartRun(Boolean taskStartRun) {
            this.taskStartRun = taskStartRun;
        }

        public Boolean getTaskRunSuccess() {
            return taskRunSuccess;
        }

        public void setTaskRunSuccess(Boolean taskRunSuccess) {
            this.taskRunSuccess = taskRunSuccess;
        }

        public Long getTaskRunMillis() {
            return taskRunMillis;
        }

        public void setTaskRunMillis(Long taskRunMillis) {
            this.taskRunMillis = taskRunMillis;
        }
    }

    @Resource
    private ConfigReadService configReadService;

    private ExecutorService pool;

    @PostConstruct
    private void init() {
        pool = new ThreadPoolExecutor(configReadService.getDHTParamAlpha() * 4, 100,
                30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * 开启独立线程执行任务，最长阻塞时间为超时时间。
     * @param task 任务匿名类
     * @param timeoutMillis 设定超时时间
     * @param state 任务数据容器
     * @param <V> 返回值类型
     * @return 超时之前任务执行完则返回任务返回值，超时则返回null
     */
    public <V> V runTaskInTime(Callable<V> task, Long timeoutMillis, TaskRunState state) {
        Long current = System.currentTimeMillis();
        Future<V> future = pool.submit(task);
        if (state == null) {
            state = new TaskRunState();
        }
        state.setTaskStartRun(true);
        try {
            V result = future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            state.setTaskRunSuccess(future.isDone());
            state.setTaskRunMillis(System.currentTimeMillis() - current);
            return result;
        } catch (Exception e) {
            state.setTaskRunSuccess(false);
            state.setTaskRunMillis(timeoutMillis);
            return null;
        }
    }
}
