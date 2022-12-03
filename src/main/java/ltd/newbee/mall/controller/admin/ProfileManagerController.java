package ltd.newbee.mall.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.AdminUser;
import ltd.newbee.mall.core.service.AdminUserService;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.util.security.Md5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("admin/profile")
public class ProfileManagerController extends BaseController {

    private static final String PREFIX = "admin/profile";

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public String profile(HttpServletRequest request) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser adminUser = adminUserService.getById(loginUserId);
        if (adminUser == null) {
            return "admin/login";
        }
        request.setAttribute("path", "profile");
        request.setAttribute("loginUserName", adminUser.getLoginUserName());
        request.setAttribute("nickName", adminUser.getNickName());
        return PREFIX + "/profile";
    }

    @PostMapping("/password")
    @ResponseBody
    public String passwordUpdate(HttpServletRequest request,
                                 @RequestParam("originalPassword") String originalPassword,
                                 @RequestParam("newPassword") String newPassword) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser adminUser = adminUserService.getById(loginUserId);
        String hash = Md5Utils.hash(originalPassword);
        if (adminUser == null || !adminUser.getLoginPassword().equals(hash)) {
            throw new BusinessException("原密码输入错误");
        }
        if (adminUserService.update()
                .set("login_password", Md5Utils.hash(newPassword))
                .eq("admin_user_id", loginUserId)
                .update()) {
            //修改成功后清空session中的数据，前端控制跳转至登录页
            request.getSession().removeAttribute("loginUserId");
            request.getSession().removeAttribute("loginUser");
            request.getSession().removeAttribute("errorMsg");
            return "success";
        } else {
            return "修改失败";
        }
    }

    @PostMapping("/name")
    @ResponseBody
    public String nameUpdate(HttpServletRequest request,
                             @RequestParam("loginUserName") String loginUserName,
                             @RequestParam("nickName") String nickName) {
        Integer loginUserId = (int) request.getSession().getAttribute("loginUserId");
        AdminUser adminUser = adminUserService.getById(loginUserId);
        if (adminUser == null) {
            throw new BusinessException("服务器内部错误");
        }
        if (adminUserService.update()
                .set("login_user_name", loginUserName)
                .set("nick_name", nickName)
                .eq("admin_user_id", loginUserId)
                .update()) {
            // 修改成功后清空session中的数据，前端控制跳转至登录页
            request.getSession().setAttribute("loginUser", nickName);
            return "success";
        } else {
            return "修改失败";
        }
    }
}
