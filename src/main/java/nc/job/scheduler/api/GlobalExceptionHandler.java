package nc.job.scheduler.api;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler
    public String exception(MethodArgumentNotValidException e){
        return Objects.requireNonNull(e.getFieldError()).getDefaultMessage();
    }
}
