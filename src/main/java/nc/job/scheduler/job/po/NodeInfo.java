package nc.job.scheduler.job.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
@Data
@Entity
@Table(name = "node_info")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NodeInfo {
    @Id
    @Column(length = 32)
    private String id;
    @Column
    private Date updateTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        NodeInfo nodeInfo = (NodeInfo) o;

        return new EqualsBuilder().append(id, nodeInfo.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).toHashCode();
    }
}
