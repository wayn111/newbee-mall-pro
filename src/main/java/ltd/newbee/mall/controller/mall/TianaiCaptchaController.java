package ltd.newbee.mall.controller.mall;

import cloud.tianai.captcha.slider.SliderCaptchaApplication;
import cloud.tianai.captcha.template.slider.validator.common.model.dto.SliderCaptchaTrack;
import cloud.tianai.captcha.vo.CaptchaResponse;
import cloud.tianai.captcha.vo.SliderCaptchaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * tianai-captcha接口
 */
@Slf4j
@Controller
@RequestMapping("tianai")
public class TianaiCaptchaController {

    @Autowired
    private SliderCaptchaApplication sliderCaptchaApplication;

    @GetMapping("gen")
    @ResponseBody
    public CaptchaResponse<SliderCaptchaVO> genCaptcha(HttpServletRequest request) {
        return sliderCaptchaApplication.generateSliderCaptcha();
    }

    @PostMapping("check")
    @ResponseBody
    public boolean checkCaptcha(@RequestParam("id") String id,
                                @RequestBody SliderCaptchaTrack sliderCaptchaTrack,
                                HttpServletRequest request) {
        return sliderCaptchaApplication.matching(id, sliderCaptchaTrack);
    }


}
