package ltd.newbee.mall.controller.mall;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.MallUser;
import ltd.newbee.mall.core.entity.vo.MallUserVO;
import ltd.newbee.mall.core.service.MallUserService;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.http.HttpUtil;
import ltd.newbee.mall.util.security.Md5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class MallUserController extends BaseController {

    @Autowired
    private MallUserService mallUserService;

    @GetMapping("/personal")
    public String personalPage(HttpServletRequest request) {
        request.setAttribute("path", "personal");
        return "mall/personal";
    }

    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.removeAttribute(Constants.MALL_USER_SESSION_KEY);
        return "mall/login";
    }

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request) {
        if (HttpUtil.isAjax(request)) {
            throw new BusinessException("请先登陆！");
        }
        return "mall/login";
    }

    @ResponseBody
    @PostMapping("/login")
    public R doLogin(MallUserVO mallUserVO,
                     // @RequestParam("verifyCode") String verifyCode,
                     @RequestParam("destPath") String destPath,
                     HttpSession session) {
        R success = R.success();
        MallUser user = mallUserService.getOne(Wrappers.<MallUser>lambdaQuery()
                .eq(MallUser::getLoginName, mallUserVO.getLoginName())
                .eq(MallUser::getPasswordMd5, Md5Utils.hash(mallUserVO.getPassword())));
        if (user == null) {
            return R.error("账户名称或者密码错误");
        }
        if (user.getLockedFlag() == 1) {
            return R.error("该账户已被禁用");
        }
        BeanUtils.copyProperties(user, mallUserVO);
        session.setAttribute(Constants.MALL_USER_SESSION_KEY, mallUserVO);
        if (StringUtils.isNotEmpty(destPath) && StringUtils.contains(destPath, "=")) {
            success.add("destPath", destPath.split("=")[1].substring(1));
        }
        return success;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "mall/register";
    }

    @ResponseBody
    @PostMapping("/register")
    public R register(@RequestParam("loginName") String loginName,
                      @RequestParam("verifyCode") String verifyCode,
                      @RequestParam("password") String password,
                      HttpSession session) {
        String kaptchaCode = (String) session.getAttribute(Constants.MALL_VERIFY_CODE_KEY);
        if (!StringUtils.equalsIgnoreCase(verifyCode, kaptchaCode)) {
            return R.error("验证码错误");
        }
        List<MallUser> list = mallUserService.list(new QueryWrapper<MallUser>()
                .eq("login_name", loginName));
        if (CollectionUtils.isNotEmpty(list) && list.size() != 1) {
            return R.error("该账户名已存在");
        }
        return R.result(mallUserService.register(loginName, password));
    }

    @PostMapping("/personal/updateInfo")
    @ResponseBody
    public R updateInfo(@RequestBody MallUser mallUser) {
        if (!mallUserService.updateById(mallUser)) {
            throw new BusinessException("修改用户信息异常");
        }
        MallUser user = mallUserService.getById(mallUser.getUserId());
        MallUserVO mallUserVO = new MallUserVO();
        BeanUtils.copyProperties(user, mallUserVO);
        session.setAttribute(Constants.MALL_USER_SESSION_KEY, mallUserVO);
        return R.success();
    }
}
