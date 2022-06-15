package nc.job.scheduler.job;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.job.scheduler.job.dao.JobInfoDao;
import nc.job.scheduler.job.dao.JobParamDao;
import nc.job.scheduler.job.dto.JobDesc;
import nc.job.scheduler.job.dto.Param;
import nc.job.scheduler.job.po.JobInfo;
import nc.job.scheduler.job.po.JobParam;
import nc.job.scheduler.job.po.JobStatus;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 任务调度器
 */
@Component
@Slf4j
@ConfigurationProperties(prefix = "job")
@Transactional
@AllArgsConstructor
public class Scheduler{
    private final JobInfoDao jobInfoDao;
    private final JobParamDao jobParamDao;
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
                .execDate(DateUtils.addSeconds(new Date(),desc.getInterval()))
                .maxRun(desc.getMaxRun())
                .status(JobStatus.Sleeping)
                .build();

        jobInfoDao.save(jobInfo);
        List<JobParam> params =desc.getParams().stream().map(param -> JobParam.builder()
                .id(UUID.randomUUID().toString())
                .value(param.getValue())
                .jobInfo(jobInfo)
                .key(param.getKey())
                .build()).collect(Collectors.toList());
        jobParamDao.saveAll(params);
    }
    @Transactional
    public void delete(String job){
        Optional<JobInfo> optional =jobInfoDao.findById(job);
        if(optional.isPresent()){
            jobInfoDao.delete(optional.get());
            jobParamDao.deleteJobParamByJobInfo(optional.get());
        }
    }
    /**
     * 周期性清理超时任务
     */
    @Scheduled(fixedDelay = 30000,initialDelay = 0)
    public void afterPropertiesSet(){
        try{
            jobInfoDao.interruptTask(new Date());
        }catch (Exception e){
            log.info("任务中断失败",e);
        }
    }
}

