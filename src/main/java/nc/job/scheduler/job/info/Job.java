package nc.job.scheduler.job.info;

import nc.job.scheduler.job.dto.JobParamDesc;

import java.util.List;

public interface Job {
    void execute(Context context);
    List<JobParamDesc> getParamDescList();
}
