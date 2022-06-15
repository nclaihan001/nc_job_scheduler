package nc.job.scheduler.job;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nc.job.scheduler.job.dao.JobInfoDao;
import nc.job.scheduler.job.info.Context;
import nc.job.scheduler.job.info.Job;
import nc.job.scheduler.job.po.JobInfo;
import nc.job.scheduler.job.po.JobParam;
import nc.job.scheduler.job.po.JobStatus;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.ProxyUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务调度池
 */
@Component
@AllArgsConstructor
@Slf4j
public class SchedulerPool implements InitializingBean {
    private static final int TASK_COUNT = 5;
    /**
     * 任务3分钟没有执行完毕视为超时
     */
    private static final int TIME_OUT = 10;
    private static final LinkedBlockingQueue<JobInfo> JOB_INFOS = new LinkedBlockingQueue<>();
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(TASK_COUNT,new CustomizableThreadFactory("SchedulerPool-"));
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(TASK_COUNT,new CustomizableThreadFactory("SchedulerPool-"));
    private final JobInfoDao jobInfoDao;
    private final ApplicationContext context;
    @Transactional
    @Scheduled(fixedDelay = 5000,initialDelay = 0)
    public void run(){
        //获取执行的任务列表
        List<JobInfo> jobInfos = jobInfoDao.findSleepingTask(new Date());
        jobInfos = jobInfos.subList(0,Math.min(TASK_COUNT,jobInfos.size()));
        for (JobInfo jobInfo : jobInfos) {
            log.info("任务[{}]添加进调度: {}",jobInfo.getName(),JOB_INFOS.offer(jobInfo));
        }
    }

    /**
     * 执行任务
     * @param jobInfo
     */
    @Transactional
    @SneakyThrows
    public void runTask(JobInfo jobInfo){
        jobInfo.setStatus(JobStatus.Running);
        jobInfo.setInterruptDate(DateUtils.addSeconds(new Date(),jobInfo.getInterval()));
        jobInfoDao.saveAndFlush(jobInfo);
        Job job = (Job) context.getBean(Class.forName(jobInfo.getClazz()));
        Map<String,String> params =jobInfo.getParams().stream()
                .collect(Collectors.toMap(JobParam::getKey, JobParam::getValue));
        Context context = new Context();
        context.setParams(params);
        try{
            jobInfo.setRun(jobInfo.getRun()+1);
            job.execute(context);
        }catch (Exception e){
            jobInfo.setFailedRun(jobInfo.getFailedRun()+1);
        }
        if(jobInfo.getRun()>= jobInfo.getMaxRun()){
            jobInfo.setStatus(JobStatus.Completed);
            log.info("任务[{}]结束执行",jobInfo.getName());
        }else{
            jobInfo.setStatus(JobStatus.Sleeping);
        }
        jobInfoDao.saveAndFlush(jobInfo);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (int i = 0; i < TASK_COUNT; i++) {
            SCHEDULED_EXECUTOR_SERVICE.scheduleWithFixedDelay(()->{
                JobInfo jobInfo =JOB_INFOS.poll();
                if(Objects.isNull(jobInfo)){
                    return;
                }
                Future<?> future= EXECUTOR_SERVICE.submit(()->context.getBean(SchedulerPool.class).runTask(jobInfo));
                try{
                    future.get(TIME_OUT,TimeUnit.SECONDS);
                }catch (TimeoutException e){
                    log.error("任务[{}]中断: {}",jobInfo.getName(),
                            future.cancel(true));
                }catch (InterruptedException | ExecutionException e){
                    log.error("任务[{}]执行失败",jobInfo.getName(),e);
                }
            },0,1, TimeUnit.SECONDS);
        }
    }
}

