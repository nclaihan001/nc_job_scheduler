package nc.job.scheduler.job.dao;

import nc.job.scheduler.job.po.JobInfo;
import nc.job.scheduler.job.po.JobParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobParamDao extends JpaRepository<JobParam,String> {
    void deleteJobParamByJobInfo(JobInfo jobInfo);
}
