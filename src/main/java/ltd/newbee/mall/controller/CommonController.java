package ltd.newbee.mall.controller;

import com.alibaba.fastjson.JSONObject;
import com.wf.captcha.ArithmeticCaptcha;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.config.NewbeeMallConfig;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.ServletUtil;
import ltd.newbee.mall.util.file.FileUploadUtil;
import ltd.newbee.mall.util.file.FileUtils;
import ltd.newbee.mall.util.http.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("common")
public class CommonController extends BaseController {

    @Autowired
    private NewbeeMallConfig newbeeMallConfig;

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            String uploadDir = newbeeMallConfig.getUploadDir();
            if (!FileUtils.isValidFilename(fileName)) {
                throw new BusinessException("文件名称(" + fileName + ")非法，不允许下载。 ");
            }

            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = uploadDir + File.separatorChar + fileName;

            response.setCharacterEncoding("utf-8");
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition",
                    "attachment;fileName=" + FileUtils.setFileDownloadHeader(request, realFileName));
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete) {
                FileUtils.deleteFile(filePath);
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求
     */
    @PostMapping("/upload")
    @ResponseBody
    public R uploadFile(MultipartFile file, HttpServletRequest request) {
        try {
            // 上传文件路径
            String fileName = FileUploadUtil.uploadFile(file, newbeeMallConfig.getUploadDir());
            String requestUrl = HttpUtil.getRequestContext(request);
            String url = requestUrl + "/upload/" + fileName;
            return R.success().add("url", url).add("fileName", fileName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error(e.getMessage());
        }
    }

    @PostMapping("/froalaUpload")
    @ResponseBody
    public void froalaUploadFile(MultipartFile file, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 上传文件路径
            String fileName = FileUploadUtil.uploadFile(file, newbeeMallConfig.getUploadDir());
            String requestUrl = HttpUtil.getRequestContext(request);
            String url = requestUrl + "/upload/" + fileName;
            Map<String, Object> data = new HashMap<>();
            data.put("link", url);
            ServletUtil.renderString(response, JSONObject.toJSONString(data));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        // 算术类型
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 48);
        // CustomArithmeticCaptcha captcha = new CustomArithmeticCaptcha(130, 48);
        // 几位数运算，默认是两位
        captcha.setLen(2);
        // 获取运算的公式：3+2=?
        captcha.getArithmeticString();
        // 获取运算的结果：5
        captcha.text();
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskInfo().getTimeMillis());

        // 验证码存入session
        request.getSession().setAttribute(Constants.MALL_VERIFY_CODE_KEY, captcha.text().toLowerCase());

        // 输出图片流
        captcha.out(response.getOutputStream());

    }

}
