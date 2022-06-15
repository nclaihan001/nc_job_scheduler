package nc.job.scheduler.job.dto;


import lombok.Data;
import nc.job.scheduler.job.po.JobStatus;

import java.util.Date;

@Data
public class JobResult {
    private String name;
    private JobStatus status;
    /**
     * 执行次数
     */
    private int run;
    /**
     * 最大执行次数
     */
    private int maxRun;
    /**
     * 失败的执行次数
     */
    private int failedRun;
    /**
     * 执行时间
     */
    private Date execDate;
    /**
     * 执行间隔
     */
    private int interval;

}
