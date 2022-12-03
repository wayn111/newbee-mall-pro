package ltd.newbee.mall.controller.base;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.core.entity.BaseEntity;
import ltd.newbee.mall.interceptor.threadlocal.AdminLoginThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Slf4j
public class BaseController {

    @Autowired
    protected HttpSession session;

    @Autowired
    protected ServletContext application;


    /**
     * <p>
     * 获取分页对象
     * </p>
     */
    protected <T> Page<T> getPage(int pageNumber) {
        return getPage(pageNumber, 15);
    }

    /**
     * <p>
     * 获取分页对象
     * </p>
     *
     * @param pageNumber 当前页
     * @param pageSize   分页数量
     * @return 分页对象
     */
    protected <T> Page<T> getPage(int pageNumber, int pageSize) {
        return new Page<>(pageNumber, pageSize);
    }

    protected <T> Page<T> getPage(HttpServletRequest request) {
        String pageNumber1 = request.getParameter(Constants.PAGE_NUMBER);
        long pageNumber = Long.parseLong(StringUtils.isNotEmpty(pageNumber1) ? pageNumber1 : "1");
        String pageSize1 = request.getParameter(Constants.PAGE_SIZE);
        long pageSize = Long.parseLong(StringUtils.isNotEmpty(pageSize1) ? pageSize1 : "10");
        return getPage(request, pageNumber, pageSize);
    }

    private <T> Page<T> getPage(HttpServletRequest request, long pageNumber, long pageSize) {
        String sortName = request.getParameter("sidx");
        String sortOrder = request.getParameter("order");
        Page<T> tPage = new Page<>(pageNumber, pageSize);
        if (StringUtils.isNotEmpty(sortName)) {
            OrderItem orderItem = new OrderItem();
            orderItem.setColumn(sortName.replaceAll("[A-Z]", "_$0").toLowerCase());
            if (Constants.ORDER_DESC.equals(sortOrder)) {
                orderItem.setAsc(false);
            }
            tPage.addOrder(orderItem);
        }
        return tPage;
    }

    protected <T> Page<T> getPage(HttpServletRequest request, long pageSize) {
        String pageNumber1 = request.getParameter(Constants.PAGE_NUMBER);
        long pageNumber = Long.parseLong(StringUtils.isNotEmpty(pageNumber1) ? pageNumber1 : "1");
        return getPage(request, pageNumber, pageSize);
    }


    /**
     * 重定向至地址 url
     *
     * @param url 请求地址
     * @return 重定向url
     */
    protected String redirectTo(String url) {
        return "redirect:" + url;
    }

    /**
     * 基本字段操作，填充当前用户Id，和创建时间
     */
    protected <T extends BaseEntity> void baseFieldHandle(T t, boolean isCreate) {
        if (isCreate) {
            t.setCreateUser(AdminLoginThreadLocal.get());
            t.setCreateTime(new Date());
        } else {
            t.setUpdateUser(AdminLoginThreadLocal.get());
            t.setUpdateTime(new Date());
        }
    }
}
