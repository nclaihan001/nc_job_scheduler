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

@Data
@Entity
@Table(name = "job_param")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobParam {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "param_key")
    private String key;
    @Column(name = "param_value")
    private String value;
    @JoinColumn(name = "job_id")
    @ManyToOne
    private JobInfo jobInfo;
}
