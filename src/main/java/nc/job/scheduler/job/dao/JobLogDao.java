package nc.job.scheduler.job.dao;

import lombok.Data;
import nc.job.scheduler.job.po.JobInfo;
import nc.job.scheduler.job.po.JobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogDao extends JpaRepository<JobLog,String> {
    void deleteAllByJobInfo(JobInfo jobInfo);
}
