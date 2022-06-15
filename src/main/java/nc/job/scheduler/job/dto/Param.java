package nc.job.scheduler.job.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Param {
    @NotBlank(message = "参数名不能为空")
    @Schema(description = "参数名")
    private String key;
    @NotNull(message = "内容不能为空")
    @Schema(description = "内容")
    private String value;
}
