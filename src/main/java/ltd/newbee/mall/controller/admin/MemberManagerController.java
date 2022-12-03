package ltd.newbee.mall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.core.entity.MallUser;
import ltd.newbee.mall.core.service.MallUserService;
import ltd.newbee.mall.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("admin/users")
public class MemberManagerController extends BaseController {

    private static final String PREFIX = "admin/member";

    @Autowired
    private MallUserService mallUserService;

    @GetMapping
    public String index(HttpServletRequest request) {
        request.setAttribute("path", "users");
        return PREFIX + "/member";
    }

    @ResponseBody
    @GetMapping("/list")
    public IPage<MallUser> list(MallUser mallUser, HttpServletRequest request) {
        Page<MallUser> page = getPage(request);
        return mallUserService.selectPage(page, mallUser);
    }

    /**
     * 用户禁用与解除禁用(0-未锁定 1-已锁定)
     */
    @RequestMapping(value = "/lock/{lockStatus}", method = RequestMethod.POST)
    @ResponseBody
    public R delete(@RequestBody List<Integer> ids, @PathVariable int lockStatus) {
        if (ids.size() < 1) {
            return R.error("参数异常！");
        }
        if (lockStatus != 0 && lockStatus != 1) {
            return R.error("操作非法！");
        }
        boolean update = mallUserService.update().set("locked_flag ", lockStatus).in("user_id", ids).update();
        return R.result(update);
    }
}
