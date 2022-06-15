package nc.job.scheduler.job.dao;

import nc.job.scheduler.job.po.JobInfo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;

@Repository
public interface JobInfoDao extends JpaRepository<JobInfo,String> {
    @Query(value = "UPDATE job_info SET status = 0,node=null WHERE status = ?2 AND node = ?1",nativeQuery = true)
    @Modifying
    @Transactional
    int interruptTask(String node,int status);


    /**
     * 获取待执行的任务，为了防止多节点重复执行，这里采用的表锁
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "FROM JobInfo WHERE status = 0 AND run < maxRun AND execDate < ?1 ORDER BY name ASC")
    List<JobInfo> findSleepingTask(Date date, Pageable pageable);


}
