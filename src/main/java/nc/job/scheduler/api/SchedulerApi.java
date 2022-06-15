package nc.job.scheduler.api;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import nc.job.scheduler.job.Scheduler;
import nc.job.scheduler.job.dto.JobClazz;
import nc.job.scheduler.job.dto.JobDesc;
import nc.job.scheduler.job.info.Job;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class SchedulerApi {
    private final Scheduler scheduler;
    private final ApplicationContext context;
    @Operation(summary = "获取可执行的任务类")
    @GetMapping(value = "task_clazz_list",produces = MediaType.APPLICATION_JSON_VALUE)
    public List<JobClazz> jobs(){
        return context.getBeansOfType(Job.class).values().stream()
                .map(job -> JobClazz.builder()
                        .clazz(job.getClass().getName())
                        .params(job.getParamDescList())
                        .build()).collect(Collectors.toList());
    }
    @Operation(summary = "创建执行任务")
    @PostMapping(value = "create_job",produces = MediaType.APPLICATION_JSON_VALUE)
    public void createJob(@RequestBody @Valid JobDesc jobDesc){

    }

}
