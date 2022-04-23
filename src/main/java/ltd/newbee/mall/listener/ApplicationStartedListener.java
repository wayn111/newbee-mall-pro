package ltd.newbee.mall.listener;

import cloud.tianai.captcha.slider.SliderCaptchaApplication;
import cloud.tianai.captcha.template.slider.resource.ResourceStore;
import cloud.tianai.captcha.template.slider.resource.SliderCaptchaResourceManager;
import cloud.tianai.captcha.template.slider.resource.common.model.dto.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    @Autowired
    private SliderCaptchaApplication sliderCaptchaApplication;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        SliderCaptchaResourceManager sliderCaptchaResourceManager = sliderCaptchaApplication.getSliderCaptchaResourceManager();
        ResourceStore resourceStore = sliderCaptchaResourceManager.getResourceStore();
        // 清除内置的背景图片
        resourceStore.clearResources();

        // 添加自定义背景图片
        resourceStore.addResource(new Resource("classpath", "bgimages/a.jpg"));
        resourceStore.addResource(new Resource("classpath", "bgimages/b.jpg"));
        resourceStore.addResource(new Resource("classpath", "bgimages/c.jpg"));
        resourceStore.addResource(new Resource("classpath", "bgimages/d.jpg"));
        resourceStore.addResource(new Resource("classpath", "bgimages/e.jpg"));
        resourceStore.addResource(new Resource("classpath", "bgimages/g.jpg"));
        resourceStore.addResource(new Resource("classpath", "bgimages/h.jpg"));
        resourceStore.addResource(new Resource("classpath", "bgimages/i.jpg"));
        resourceStore.addResource(new Resource("classpath", "bgimages/j.jpg"));
    }
}
