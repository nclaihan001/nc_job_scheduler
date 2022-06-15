package nc.job.scheduler;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition(
        info = @Info(title = "任务调度系统",version = "1.0.0")
)
@SpringBootApplication
public class NcJobSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NcJobSchedulerApplication.class, args);
    }

}
