package nc.job.scheduler.job;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nc.job.scheduler.job.dao.JobInfoDao;
import nc.job.scheduler.job.dao.JobLogDao;
import nc.job.scheduler.job.dao.JobParamDao;
import nc.job.scheduler.job.dto.JobDesc;
import nc.job.scheduler.job.dto.JobResult;
import nc.job.scheduler.job.po.JobInfo;
import nc.job.scheduler.job.po.JobLog;
import nc.job.scheduler.job.po.JobParam;
import nc.job.scheduler.job.po.JobStatus;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 任务调度器
 */
@Component
@Slf4j
@Transactional
@AllArgsConstructor
public class Scheduler{
    private final JobInfoDao jobInfoDao;
    private final JobParamDao jobParamDao;
    private final JobLogDao jobLogDao;

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

    /**
     * 删除任务
     * @param job
     */
    @Transactional
    public void delete(String job){
        Optional<JobInfo> optional =jobInfoDao.findById(job);
        if(optional.isPresent()){
            jobLogDao.deleteAllByJobInfo(optional.get());
            jobParamDao.deleteJobParamByJobInfo(optional.get());
            jobInfoDao.delete(optional.get());
        }
    }
    /**
     * 重启任务
     * @param job
     */
    @Transactional
    public boolean restart(String job){
        Optional<JobInfo> optional =jobInfoDao.findById(job);
        if(optional.isPresent()){
            JobInfo jobInfo = optional.get();
            jobInfo.setStatus(JobStatus.Sleeping);
            jobInfo.setRun(0);
            jobInfoDao.saveAndFlush(jobInfo);
            jobLogDao.saveAndFlush(JobLog.builder()
                            .id(UUID.randomUUID().toString().replaceAll("-",""))
                            .createdDate(new Date())
                            .jobInfo(jobInfo)
                            .desc("任务重启")
                    .build());
            return true;
        }
        return false;
    }
    /**
     * 停止任务
     * @param job
     */
    @Transactional
    public boolean stop(String job){
        Optional<JobInfo> optional =jobInfoDao.findById(job);
        if(optional.isPresent()){
            JobInfo jobInfo = optional.get();
            jobInfo.setStatus(JobStatus.Completed);
            jobInfoDao.saveAndFlush(jobInfo);
            jobLogDao.saveAndFlush(JobLog.builder()
                    .id(UUID.randomUUID().toString().replaceAll("-",""))
                    .createdDate(new Date())
                    .jobInfo(jobInfo)
                    .desc("任务停止")
                    .build());
            return true;
        }
        return false;
    }
    /**
     * 获取任务列表
     * @return
     */
    public List<JobResult> getJobs(){
        return jobInfoDao.findAll().stream().map(jobInfo -> {
            JobResult jobResult = new JobResult();
            BeanUtils.copyProperties(jobInfo,jobResult);
            jobResult.setLogs(jobInfo.getJobLogs().stream()
                    .map(jobLog ->
                            "["+ DateFormatUtils.format(jobLog.getCreatedDate(),"yyyy-MM-dd HH:mm:ss") +"] "+
                                    jobLog.getDesc()).collect(Collectors.toList()));
            return jobResult;
        }).collect(Collectors.toList());
    }

}

