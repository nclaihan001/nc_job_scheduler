package nc.job.scheduler.job.info;


import lombok.Data;

import java.util.Map;
@Data
public class Context {
    private Map<String,String> params;
}
