package nc.job.scheduler.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nc.job.scheduler.job.dao.JobInfoDao;
import nc.job.scheduler.job.dao.JobLogDao;
import nc.job.scheduler.job.dao.NodeInfoDao;
import nc.job.scheduler.job.info.Context;
import nc.job.scheduler.job.info.Job;
import nc.job.scheduler.job.po.JobInfo;
import nc.job.scheduler.job.po.JobLog;
import nc.job.scheduler.job.po.JobParam;
import nc.job.scheduler.job.po.JobStatus;
import nc.job.scheduler.job.po.NodeInfo;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 任务调度池
 */
@Component
@AllArgsConstructor
@Slf4j
@Data
public class SchedulerPool implements InitializingBean{
    private static final int TASK_COUNT = 5;
    private static final ScheduledExecutorService RUNNER_SERVICE = Executors.newScheduledThreadPool(TASK_COUNT,new CustomizableThreadFactory("RunnerPool-"));
    private static final ExecutorService WORKER_SERVICE = Executors.newFixedThreadPool(TASK_COUNT,new CustomizableThreadFactory("WorkerPool-"));
    private final JobInfoDao jobInfoDao;
    private final ApplicationContext context;
    private final TransactionTemplate transactionTemplate;
    private final NodeInfoDao nodeInfoDao;
    private final JobLogDao jobLogDao;
    @Override
    public void afterPropertiesSet() throws Exception {
        jobInfoDao.interruptTask(getNodeId(),1);
        for (int i = 0; i < TASK_COUNT; i++) {
            RUNNER_SERVICE.scheduleWithFixedDelay(new RunnerTask(),0,1, TimeUnit.SECONDS);
        }
    }

    /**
     * 每隔10秒清理一次丢失节点
     */
    @Transactional
    @Scheduled(fixedDelay = 10000,initialDelay = 5000)
    public void removeLostNode(){
        for (NodeInfo nodeInfo : nodeInfoDao.findLostNode(DateUtils.addSeconds(new Date(),-10))) {
            nodeInfoDao.delete(nodeInfo);
            jobInfoDao.interruptTask(nodeInfo.getId(),0);
            jobInfoDao.interruptTask(nodeInfo.getId(),1);
        }
    }
    /**
     * 每隔3秒注册一次节点
     */
    @Scheduled(fixedDelay = 3000,initialDelay = 5000)
    public void registerNode(){
        String nodeId = getNodeId();
        nodeInfoDao.saveAndFlush(NodeInfo.builder().id(nodeId).updateTime(new Date()).build());
    }
    private String getNodeId(){
        String nodeName = Objects.requireNonNull(context.getEnvironment().getProperty("job.name"));
        return DigestUtils.md5DigestAsHex(nodeName.getBytes(StandardCharsets.UTF_8));
    }
    class RunnerTask implements Runnable{
        @Override
        public void run() {
            List<JobInfo> jobInfos=transactionTemplate.execute(status -> {
                List<JobInfo> jobInfos1 = jobInfoDao.findSleepingTask(new Date(), PageRequest.of(0,10));
                for (JobInfo jobInfo : jobInfos1) {
                    if(jobInfo.getStatus()!=JobStatus.Sleeping){
                        continue;
                    }
                    jobInfo.setStatus(JobStatus.Running);
                    jobInfo.setNode(getNodeId());
                    jobInfoDao.saveAndFlush(jobInfo);
                    return List.of(jobInfo);
                }
                return List.of();
            });
            jobInfos = Optional.ofNullable(jobInfos).orElse(List.of());
            if(CollectionUtils.isEmpty(jobInfos)){
                return;
            }
            //获取执行的任务列表
            for (JobInfo jobInfo : jobInfos) {
                RUNNER_SERVICE.execute(()->{
                    //慢任务警告
                    Long slowTask = Objects.requireNonNull(context.getEnvironment().getProperty("job.slowTask",Long.class));
                    long startTime = System.currentTimeMillis();
                    try{
                        context.getBean(SchedulerPool.class).runTask(jobInfo);
                    }finally {
                        long interval = System.currentTimeMillis() - startTime;
                        if(interval > slowTask){
                            jobLogDao.saveAndFlush(JobLog.builder()
                                            .id(UUID.randomUUID().toString().replaceAll("-",""))
                                            .jobInfo(jobInfo)
                                            .desc("任务执行时间过长:"+interval+"ms")
                                            .createdDate(new Date())
                                    .build());
                            log.warn("任务[{}]的执行时间过长: {} ms",jobInfo.getName(),interval);
                        }
                    }
                });
            }
        }
    }
    /**
     * 执行任务
     * @param jobInfo
     */
    @SneakyThrows
    public void runTask(JobInfo jobInfo){
        Job job = (Job) context.getBean(Class.forName(jobInfo.getClazz()));
        Map<String,String> params =jobInfo.getParams().stream()
                .collect(Collectors.toMap(JobParam::getKey, JobParam::getValue));
        Context context = new Context();
        context.setParams(params);
        try{
            jobInfo.setRun(jobInfo.getRun()+1);
            job.execute(context);
            jobInfo.setExecDate(DateUtils.addSeconds(new Date(),jobInfo.getInterval()));
            jobLogDao.saveAndFlush(JobLog.builder()
                    .id(UUID.randomUUID().toString().replaceAll("-",""))
                    .jobInfo(jobInfo)
                    .desc("任务执行完成")
                    .createdDate(new Date())
                    .build());
        }catch (Exception e){
            jobInfo.setFailedRun(jobInfo.getFailedRun()+1);
            jobLogDao.saveAndFlush(JobLog.builder()
                    .id(UUID.randomUUID().toString().replaceAll("-",""))
                    .jobInfo(jobInfo)
                    .desc("任务执行失败")
                    .createdDate(new Date())
                    .build());
        }
        if(jobInfo.isOnce() || jobInfo.getRun()>=jobInfo.getMaxRun()){
            jobInfo.setStatus(JobStatus.Completed);
            log.info("任务[{}]结束执行",jobInfo.getName());
        }else{
            jobInfo.setStatus(JobStatus.Sleeping);
        }
        jobInfoDao.saveAndFlush(jobInfo);
    }
}

