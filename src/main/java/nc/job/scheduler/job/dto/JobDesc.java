package nc.job.scheduler.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 任务详情
 */
@Data
@Schema
public class JobDesc {
    @NotBlank(message = "任务名不能为空")
    @Length(max = 50,message = "任务名称不能超过50个字符")
    @Schema(description = "任务名称，不同的任务请使用不同的名称",required = true)
    private String name;
    @NotBlank(message = "任务类不能为空")
    @Length(max = 255,message = "任务类不能超过255个字符")
    @Schema(description = "执行的任务类",required = true)
    private String clazz;
    @Min(message = "执行间隔必须大于5",value = 5)
    @Schema(description = "执行间隔，单位为秒",required = true,example = "5")
    private int interval;
    @Min(message = "执行上限必须大于0",value = 1)
    @Schema(description = "执行上限",required = true,example = "1")
    private int maxRun;
    @Valid
    @Schema(description = "执行参数",required = true)
    private List<Param> params;
}
