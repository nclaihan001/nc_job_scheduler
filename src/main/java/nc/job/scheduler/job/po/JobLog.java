package nc.job.scheduler.job.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(name = "slow_log")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobLog {
    @Id
    @Column(length = 32)
    private String id;
    @ManyToOne
    @JoinColumn(name = "job_id")
    private JobInfo jobInfo;
    @Column(name = "log_desc")
    private String desc;
    @Column(name = "created_date")
    private Date createdDate;
}
