package nc.job.scheduler.custom;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nc.job.scheduler.job.dto.JobParamDesc;
import nc.job.scheduler.job.info.Context;
import nc.job.scheduler.job.info.Job;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
public class HelloWordJob implements Job {
    @SneakyThrows
    @Override
    public void execute(Context context) {
        log.info(context.getParams().get("str"));
        Thread.sleep(1000*80);
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
