package ltd.newbee.mall.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.util.file.FileUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * servlet帮助类
 */
@Slf4j
public class ServletUtil {

    private static final ServletUtil GET_INSTANCE;

    static {
        GET_INSTANCE = new ServletUtil();
    }

    /**
     * 获取requestAttributes
     *
     * @return ServletRequestAttributes
     */
    public static ServletRequestAttributes getRequestAttributes() {
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }

    /**
     * 获取当前请求request
     *
     * @return HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取当前请求response
     *
     * @return HttpServletRequest
     */
    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name) {
        return Integer.parseInt(getRequest().getParameter(name));
    }

    /**
     * 获取Integer参数
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取所有参数
     */
    public static Map<String, String[]> getAllParameter() {
        return getRequest().getParameterMap();
    }

    /**
     * 设置参数
     *
     * @param name
     * @param value
     * @return
     */
    public static ServletUtil setParameter(String name, Object value) {
        getRequest().setAttribute(name, value);
        return GET_INSTANCE;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     * @return string
     */
    public static String renderString(HttpServletResponse response, String string) {
        try {
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 设置文件导出响应流
     *
     * @param response 响应对象
     * @param request  请求对象
     * @param size     文件大小
     * @throws UnsupportedEncodingException 不支持字符编码异常
     */
    public static void setExportResponse(HttpServletRequest request, HttpServletResponse response, String fileName, Integer size) throws UnsupportedEncodingException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Length", size + "");
        response.setHeader("Content-Disposition",
                "attachment;fileName=" + FileUtils.setFileDownloadHeader(request, fileName));
        response.setContentType("application/octet-stream;charset=UTF-8");
    }
}
