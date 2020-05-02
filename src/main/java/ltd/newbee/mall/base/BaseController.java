package ltd.newbee.mall.base;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import ltd.newbee.mall.constant.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
     * @param pageNumber
     * @param pageSize
     * @param <T>
     * @return
     */
    protected <T> Page<T> getPage(int pageNumber, int pageSize) {
        return new Page<>(pageNumber, pageSize);
    }

    protected <T> Page<T> getPage(HttpServletRequest request) {
        String pageNumber1 = request.getParameter(Constants.PAGE_NUMBER);
        long pageNumber = Long.parseLong(StringUtils.isNotEmpty(pageNumber1) ? pageNumber1 : "1");
        String pageSize1 = request.getParameter(Constants.PAGE_SIZE);
        long pageSize = Long.parseLong(StringUtils.isNotEmpty(pageSize1) ? pageSize1 : "10");
        return gettPage(request, pageNumber, pageSize);
    }

    private <T> Page<T> gettPage(HttpServletRequest request, long pageNumber, long pageSize) {
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
        return gettPage(request, pageNumber, pageSize);
    }


    /**
     * 重定向至地址 url
     *
     * @param url 请求地址
     * @return
     */
    protected String redirectTo(String url) {
        StringBuffer rto = new StringBuffer("redirect:");
        rto.append(url);
        return rto.toString();
    }
}
