package ltd.newbee.mall.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.http.HttpUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice extends BaseController {


    /**
     * 处理404异常， eg：springboot中404异常会自动找到templates/error目录下的404页面文件
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handle404Exception(NoHandlerFoundException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        if (HttpUtil.isAjax(request)) {
            return R.error("您请求路径不存在，请检查url！");
        }
        return new ModelAndView("error/404");
    }

    /**
     * 处理自定义异常
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler({BusinessException.class})
    public Object handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        if (HttpUtil.isAjax(request)) {
            return R.error(e.getMessage());
        }
        Map<String, Object> map = new HashMap<>();
        map.put("msg", e.getMessage());
        return new ModelAndView("error/500", map);
    }

    /**
     * 处理文件上传过大bug
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler({MaxUploadSizeExceededException.class})
    public Object handleBusinessException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        return R.error(e.getMessage());
    }

    /**
     * 处理全局异常
     *
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler({Exception.class})
    public Object handleException(Exception e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        if (HttpUtil.isAjax(request)) {
            return R.error("服务器内部错误！");
        }
        return new ModelAndView("error/500");
    }
}
