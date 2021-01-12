package ltd.newbee.mall.controller;

import com.google.code.kaptcha.Producer;
import lombok.extern.slf4j.Slf4j;
import ltd.newbee.mall.controller.base.BaseController;
import ltd.newbee.mall.constant.Constants;
import ltd.newbee.mall.exception.BusinessException;
import ltd.newbee.mall.util.R;
import ltd.newbee.mall.util.file.FileUploadUtil;
import ltd.newbee.mall.util.file.FileUtils;
import ltd.newbee.mall.util.http.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("common")
public class CommonController extends BaseController {

    @Value("${wayn.uploadDir}")
    private String filePath;
    @Autowired
    private Producer producer;

    /**
     * 通用下载请求
     *
     * @param fileName 文件名称
     * @param delete   是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request) {
        try {
            String uploadDir = filePath;
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
    public R uploadFile(MultipartFile file, HttpServletRequest request) throws Exception {
        try {
            // 上传文件路径
            String fileName = FileUploadUtil.uploadFile(file, filePath);
            String requestUrl = HttpUtil.getRequestContext(request);
            String url = requestUrl + "/upload/" + fileName;
            return R.success().add("url", url).add("fileName", fileName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/captcha")
    public void captcha(HttpServletRequest request, HttpServletResponse response) {
        try (ServletOutputStream out = response.getOutputStream()) {
            response.setDateHeader("Expires", 0);
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            response.addHeader("Cache-Control", "post-check=0, pre-check=0");
            response.setHeader("Pragma", "no-cache");
            response.setContentType("image/jpeg");

            String capText = producer.createText();
            HttpSession session = request.getSession();
            //将验证码存入shiro 登录用户的session
            session.setAttribute(Constants.MALL_VERIFY_CODE_KEY, capText);
            BufferedImage image = producer.createImage(capText);
            ImageIO.write(image, "jpg", out);
            out.flush();
        } catch (IOException e) {
            log.error("验证码生成失败", e);
        }

    }

}
