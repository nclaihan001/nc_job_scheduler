package nc.job.scheduler.job.dao;

import nc.job.scheduler.job.po.NodeInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;

@Repository
public interface NodeInfoDao extends JpaRepository<NodeInfo,String> {
    /**
     * 获取失去连接的节点
     * @param date
     * @return
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "FROM NodeInfo WHERE updateTime < ?1")
    List<NodeInfo> findLostNode(Date date);
}
