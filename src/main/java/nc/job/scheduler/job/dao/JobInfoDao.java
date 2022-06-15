package nc.job.scheduler.job.dao;

import nc.job.scheduler.job.po.JobInfo;
import nc.job.scheduler.job.po.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobInfoDao extends JpaRepository<JobInfo,String> {
    @Query(value = "UPDATE job_info SET run = run + 1,status = 0,failed_run = failed_run + 1 WHERE status = 1 AND interrupt_date < ?1 AND interrupt_date IS NOT NULL",nativeQuery = true)
    @Modifying
    @Transactional
    int interruptTask(Date date);


    /**
     * 获取待执行的任务，为了防止多节点重复执行，这里采用的表锁
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "FROM JobInfo WHERE status = 0 AND run < maxRun AND execDate < ?1 ORDER BY name ASC")
    List<JobInfo> findSleepingTask(Date date);


}
