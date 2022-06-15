package nc.job.scheduler.job;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.job.scheduler.job.dao.JobInfoDao;
import nc.job.scheduler.job.dto.JobDesc;
import nc.job.scheduler.job.dto.Param;
import nc.job.scheduler.job.po.JobInfo;
import nc.job.scheduler.job.po.JobStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 任务调度器
 */
@Component
@Slf4j
@ConfigurationProperties(prefix = "job")
@Transactional
@AllArgsConstructor
public class Scheduler implements InitializingBean {
    private final JobInfoDao jobInfoDao;

    /**
     * 注册新任务
     * @param desc
     * @return
     */
    public void register(JobDesc desc){
        JobInfo jobInfo=JobInfo.builder()
                .name(desc.getName())
                .clazz(desc.getClazz())
                .interval(desc.getInterval())
                .run(0)
                .failedRun(0)
                .timeOut(desc.getTimeOut())
                .maxRun(desc.getMaxRun())
                .params(desc.getParams().stream().collect(Collectors.toMap(Param::getKey,Param::getValue)))
                .status(JobStatus.Sleeping)
                .build();

        jobInfoDao.save(jobInfo);
    }

    /**
     * 周期性清理超时任务
     */
    @Scheduled(fixedDelay = 30000,initialDelay = 0)
    public void afterPropertiesSet(){
        try{
            jobInfoDao.interruptTask();
            log.info("任务中断成功");
        }catch (Exception e){
            log.info("任务中断失败",e);
        }
    }
}

