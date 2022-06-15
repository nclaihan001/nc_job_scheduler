package nc.job.scheduler.job.dao;

import nc.job.scheduler.job.po.JobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface JobInfoDao extends JpaRepository<JobInfo,String> {
    @Query(value = "UPDATE job_info SET run = run + 1,status = 0,failed_run = failed_run + 1 WHERE exec_interval < NOW()",nativeQuery = true)
    @Modifying
    @Transactional
    void interruptTask();
}
