package ltd.newbee.mall.config;

import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.intercepter.AdminLoginInterceptor;
import ltd.newbee.mall.intercepter.MallLoginValidateIntercepter;
import ltd.newbee.mall.intercepter.MallShopCartNumberInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${wayn.uploadDir}")
    private String uploadDir;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/index");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** 本地文件上传路径 */
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + uploadDir + "/");
        registry.addResourceHandler("/goods-img/**").addResourceLocations("file:" + Constants.FILE_UPLOAD_DIC);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new MallLoginValidateIntercepter())
                .excludePathPatterns("/login")
                .excludePathPatterns("/logout")
                .excludePathPatterns("/register")
                .excludePathPatterns("/upload/**")
                .excludePathPatterns("/goods-img/**")
                .excludePathPatterns("/common/**")
                .excludePathPatterns("/mall/**")
                .excludePathPatterns("/admin/**");

        // 购物车中的数量统一处理
        registry.addInterceptor(mallShopCartNumberInterceptor())
                .excludePathPatterns("/admin/**")
                .excludePathPatterns("/register")
                .excludePathPatterns("/login")
                .excludePathPatterns("/logout")
                .excludePathPatterns("/**/captcha")
                .excludePathPatterns("/**/*.jpg")
                .excludePathPatterns("/**/*.png")
                .excludePathPatterns("/**/*.gif")
                .excludePathPatterns("/**/*.map")
                .excludePathPatterns("/**/*.css")
                .excludePathPatterns("/**/*.js");

        // 添加一个拦截器，拦截以/admin为前缀的url路径（后台登陆拦截）
        registry.addInterceptor(new AdminLoginInterceptor())
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login")
                .excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");
    }

    @Bean
    public MallShopCartNumberInterceptor mallShopCartNumberInterceptor() {
        return new MallShopCartNumberInterceptor();
    }
}
