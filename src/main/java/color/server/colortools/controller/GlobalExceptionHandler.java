package color.server.colortools.controller;

import color.server.colortools.entity.ResInfo;
import color.server.colortools.exception.BusinessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理控制器
 *
 * @RestControllerAdvice（@ResponseBody+@ControllerAdvise） RestControllerAdvice注解默认拦截所有的Controller的异常；
 * 如果需要指定包或者指定类进行处理，则设置对应的属性即可
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResInfo handleBussException(HttpServletRequest req, Exception ex) {
        System.out.println("handleBussException");
        if (ex instanceof BusinessException) {
            BusinessException be = (BusinessException) ex;
            return new ResInfo(true, be.getStatus(), be.getMessage());
        }
        return new ResInfo(true, ResInfo.STATUS_INNER_ERR, "未知错误");
    }

    @ExceptionHandler(Exception.class)
    public ResInfo handleException(HttpServletRequest req, Exception ex) {
        System.out.println("程序报错：" + ex.getMessage());
        ex.printStackTrace();
        return handleBussException(req, ex);
    }
}

