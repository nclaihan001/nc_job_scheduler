package nc.job.scheduler.custom;

import nc.job.scheduler.job.dto.JobParamDesc;
import nc.job.scheduler.job.info.Context;
import nc.job.scheduler.job.info.Job;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HelloWordJob implements Job {
    @Override
    public void execute(Context context) {
        
    }

    @Override
    public List<JobParamDesc> getParamDescList() {
        return List.of(
                JobParamDesc.builder()
                        .name("str")
                        .desc("打印的字符串")
                        .build()
        );
    }
}
