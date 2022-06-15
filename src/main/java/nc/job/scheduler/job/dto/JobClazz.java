package nc.job.scheduler.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
@AllArgsConstructor
public class JobClazz {
    private String clazz;
    private List<JobParamDesc> params;
}
