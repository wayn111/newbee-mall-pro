package ltd.newbee.mall.config;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import ltd.newbee.mall.interceptor.AdminLoginInterceptor;
import ltd.newbee.mall.interceptor.AdminViewModelInterceptor;
import ltd.newbee.mall.interceptor.MallLoginValidateInterceptor;
import ltd.newbee.mall.interceptor.RepeatSubmitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 默认日期时间格式
     */
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";


    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 默认时间格式
     */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    @Autowired
    private NewbeeMallConfig newbeeMallConfig;
    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/index");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 本地文件上传路径
        registry.addResourceHandler("/upload/**", "/goods-img/**").addResourceLocations("file:" + newbeeMallConfig.getUploadDir() + "/");
    }


    /**
     * Json序列化和反序列化转换器，用于转换Post请求体中的json以及将我们的对象序列化为返回响应的json
     */
    @Bean
    public ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // LocalDateTime系列序列化和反序列化模块，继承自jsr310，我们在这里修改了日期格式
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class,
                new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(
                DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class,
                new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));

        // Date序列化和反序列化
        javaTimeModule.addSerializer(Date.class, new JsonSerializer<>() {
            @Override
            public void serialize(Date date, JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeString(DateUtil.formatDate(date));
            }
        });
        javaTimeModule.addDeserializer(Date.class, new JsonDeserializer<>() {
            @Override
            public Date deserialize(JsonParser jsonParser,
                                    DeserializationContext deserializationContext) throws IOException {
                return DateUtil.parse(jsonParser.getText());
            }
        });

        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 商城前台登录拦截器
        registry.addInterceptor(new MallLoginValidateInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/", "/index", "/login", "/logout", "/register",
                        "/search", "/coupon", "/goods/**", "/shopCart/getUserShopCartCount",
                        "/seckill/list", "/seckill/detail/*", "/seckill/time/now", "/seckill/*/exposer",
                        "/upload/**", "/goods-img/**", "/common/**", "/mall/**", "/admin/**", "/tianai/**"
                );


        // 添加一个拦截器，拦截以/admin为前缀的url路径（后台登陆拦截）
        registry.addInterceptor(new AdminLoginInterceptor()).addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login").excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");

        // 防止重复提交拦截器
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");

        // 后台演示模式拦截器
        registry.addInterceptor(new AdminViewModelInterceptor(newbeeMallConfig))
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login").excludePathPatterns("/admin")
                .excludePathPatterns("/admin/statistics").excludePathPatterns("/admin/goods")
                .excludePathPatterns("/admin/goods/add").excludePathPatterns("/admin/goods/edit/*")
                .excludePathPatterns("/admin/goods/list").excludePathPatterns("/admin/users")
                .excludePathPatterns("/admin/users/list").excludePathPatterns("/admin/carousels")
                .excludePathPatterns("/admin/carousels/list").excludePathPatterns("/admin/indexConfigs")
                .excludePathPatterns("/admin/indexConfigs/list").excludePathPatterns("/admin/categories")
                .excludePathPatterns("/admin/categories/list").excludePathPatterns("/admin/orders")
                .excludePathPatterns("/admin/categories/listForSelect").excludePathPatterns("/admin/orders")
                .excludePathPatterns("/admin/orders/list").excludePathPatterns("/admin/coupon")
                .excludePathPatterns("/admin/coupon/list").excludePathPatterns("/admin/seckill")
                .excludePathPatterns("/admin/seckill/list").excludePathPatterns("/admin/profile")
                .excludePathPatterns("/admin/logout").excludePathPatterns("/admin/dist/**")
                .excludePathPatterns("/admin/plugins/**");
    }

    public static void main(String[] args) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        System.out.println(
                antPathMatcher.match("/goods-img/**", "/goods-img/040a3aa6-1699-4eca-ac67-5021cc419979.jpg"));

    }

}
