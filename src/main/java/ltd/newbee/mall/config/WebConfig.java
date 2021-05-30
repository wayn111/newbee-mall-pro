package ltd.newbee.mall.config;

import ltd.newbee.mall.interceptor.AdminLoginInterceptor;
import ltd.newbee.mall.interceptor.AdminViewModelIntercepter;
import ltd.newbee.mall.interceptor.MallLoginValidateInterceptor;
import ltd.newbee.mall.interceptor.RepeatSubmitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${wayn.uploadDir}")
    private String uploadDir;

    @Value("${wayn.viewModel}")
    private boolean viewModel;

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/index");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** 本地文件上传路径 */
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + uploadDir + "/");
        registry.addResourceHandler("/goods-img/**").addResourceLocations("file:" + uploadDir + "/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MallLoginValidateInterceptor())
                .excludePathPatterns("/login")
                .excludePathPatterns("/logout")
                .excludePathPatterns("/")
                .excludePathPatterns("/index")
                .excludePathPatterns("/search")
                .excludePathPatterns("/coupon")
                .excludePathPatterns("/goods/**")
                .excludePathPatterns("/shopCart/getUserShopCartCount")
                .excludePathPatterns("/seckill/list")
                .excludePathPatterns("/seckill/detail/*")
                .excludePathPatterns("/seckill/time/now")
                .excludePathPatterns("/seckill/*/exposer")
                .excludePathPatterns("/register")
                .excludePathPatterns("/upload/**")
                .excludePathPatterns("/goods-img/**")
                .excludePathPatterns("/common/**")
                .excludePathPatterns("/mall/**")
                .excludePathPatterns("/admin/**");

        // 添加一个拦截器，拦截以/admin为前缀的url路径（后台登陆拦截）
        registry.addInterceptor(new AdminLoginInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");

        registry.addInterceptor(repeatSubmitInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(new AdminViewModelIntercepter(viewModel))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin")
                .excludePathPatterns("/admin/statistics")
                .excludePathPatterns("/admin/goods")
                .excludePathPatterns("/admin/goods/add")
                .excludePathPatterns("/admin/goods/edit/*")
                .excludePathPatterns("/admin/goods/list")
                .excludePathPatterns("/admin/users")
                .excludePathPatterns("/admin/users/list")
                .excludePathPatterns("/admin/carousels")
                .excludePathPatterns("/admin/carousels/list")
                .excludePathPatterns("/admin/indexConfigs")
                .excludePathPatterns("/admin/indexConfigs/list")
                .excludePathPatterns("/admin/categories")
                .excludePathPatterns("/admin/categories/list")
                .excludePathPatterns("/admin/orders")
                .excludePathPatterns("/admin/orders/list")
                .excludePathPatterns("/admin/coupon")
                .excludePathPatterns("/admin/coupon/list")
                .excludePathPatterns("/admin/seckill")
                .excludePathPatterns("/admin/seckill/list")
                .excludePathPatterns("/admin/profile")
                .excludePathPatterns("/admin/logout")
                .excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");
    }

}
