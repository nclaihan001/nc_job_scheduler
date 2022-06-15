package nc.job.scheduler.job.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@Entity
@Table(name = "job_info")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobInfo {
    /**
     * 任务名称
     */
    @Id
    @Column(length = 50)
    private String name;
    /**
     * 任务状态
     */
    @Column
    @Enumerated(value = EnumType.ORDINAL)
    private JobStatus status;
    /**
     * 执行次数
     */
    @Column
    private int run;
    /**
     * 最大执行次数
     */
    @Column
    private int maxRun;
    /**
     * 失败的执行次数
     */
    @Column
    private int failedRun;
    /**
     * 执行时间
     */
    @Column(nullable = true)
    private Date execDate;
    /**
     * 执行间隔
     */
    @Column(name = "exec_interval")
    private int interval;
    /**
     * 执行类
     */
    @Column(name = "clazz")
    private String clazz;
    @Column
    private boolean once;
    /**
     * 执行节点
     */
    @Column
    private String node;
    @OneToMany(mappedBy = "jobInfo",fetch = FetchType.EAGER)
    private List<JobParam> params;
    /**
     * 任务日志
     */
    @OrderBy(value = "createdDate asc")
    @OneToMany(mappedBy = "jobInfo",fetch = FetchType.LAZY)
    private List<JobLog> jobLogs;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof JobInfo) {
            JobInfo jobInfo = (JobInfo) o;
            return StringUtils.equals(jobInfo.name,name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
