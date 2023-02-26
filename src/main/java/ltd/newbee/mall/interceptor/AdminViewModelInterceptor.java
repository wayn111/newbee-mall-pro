package ltd.newbee.mall.interceptor;

import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ltd.newbee.mall.config.NewbeeMallConfig;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.ServletUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 演示模式拦截器
 */
public class AdminViewModelInterceptor implements HandlerInterceptor {

    private final NewbeeMallConfig newbeeMallConfig;


    public AdminViewModelInterceptor(NewbeeMallConfig newbeeMallConfig) {
        this.newbeeMallConfig = newbeeMallConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) {
        if (newbeeMallConfig.getViewModel()) {
            ServletUtil.renderString(response, JSONObject.toJSONString(R.error(newbeeMallConfig.getViewModelTip())));
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
