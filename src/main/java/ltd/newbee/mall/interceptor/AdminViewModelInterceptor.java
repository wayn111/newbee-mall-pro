package ltd.newbee.mall.interceptor;

import com.alibaba.fastjson.JSONObject;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.ServletUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 演示模式拦截器
 */
public class AdminViewModelInterceptor implements HandlerInterceptor {

    private final boolean viewModel;

    public AdminViewModelInterceptor(boolean viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o) throws Exception {
        if (viewModel) {
            ServletUtil.renderString(response, JSONObject.toJSONString(R.error("请下载项目源代码，演示模式无法修改！")));
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
