package com.community.controller;

import com.community.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传控制器 - 处理图片等文件的接收和存储
 * <p>
 * 映射路径：{@code /api/upload}<br>
 * 所属模块：公共服务模块<br>
 * 开放范围：已登录的普通用户（需要有效的JWT令牌）
 * </p>
 * <p>
 * 上传流程：
 * <ol>
 *   <li>前端通过表单上传文件</li>
 *   <li>后端校验文件类型和大小</li>
 *   <li>使用UUID重命名文件防止冲突</li>
 *   <li>保存到本地磁盘指定目录</li>
 *   <li>返回可访问的文件URL路径</li>
 * </ol>
 * </p>
 * <p>
 * <b>配置说明：</b>上传根目录通过 {@code upload.path} 配置项设置，默认值为 {@code uploads/}。<br>
 * 实际存储路径为：{项目目录}/uploads/images/。
 * </p>
 *
 * @author community-platform
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    /** 文件上传根路径，从配置文件中读取，默认为 uploads/ */
    @Value("${upload.path:uploads/}")
    private String uploadPath;

    /**
     * 上传图片文件
     * <p>
     * POST /api/upload/image<br>
     * 接收前端上传的图片文件，经过安全校验后保存到本地磁盘，<br>
     * 返回图片的相对访问路径供前端使用。
     * </p>
     * <p>
     * <b>安全校验规则：</b>
     * <ul>
     *   <li>文件名不能为空</li>
     *   <li>仅支持 jpg、jpeg、png 三种图片格式</li>
     *   <li>文件大小不能超过 5MB</li>
     * </ul>
     * </p>
     * <p>
     * <b>存储规则：</b>
     * <ul>
     *   <li>文件名：UUID + 原始后缀，确保唯一性</li>
     *   <li>存储路径：{项目目录}/uploads/images/{文件名}</li>
     *   <li>访问URL：/uploads/images/{文件名}</li>
     * </ul>
     * </p>
     *
     * @param file 前端上传的图片文件（表单参数名：file）
     * @return 成功时返回图片访问URL（如"/uploads/images/xxx.jpg"）；校验不通过或IO异常时返回错误
     */
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 校验文件名不能为空
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                return Result.error("文件名不能为空");
            }

            // 校验文件类型（仅允许常见图片格式）
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!".jpg".equals(suffix) && !".jpeg".equals(suffix) && !".png".equals(suffix)) {
                return Result.error("只支持 jpg、jpeg、png 格式");
            }

            // 校验文件大小（最大5MB）
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("文件大小不能超过 5MB");
            }

            // 使用UUID生成唯一文件名，防止文件名冲突
            String fileName = UUID.randomUUID().toString() + suffix;

            // 构建存储目录的绝对路径并确保目录存在
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + File.separator + "uploads" + File.separator + "images";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
                log.info("创建上传目录: {}", dir.getAbsolutePath());
            }

            // 将上传的文件保存到目标路径
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            log.info("图片上传成功: {}, 文件大小: {} bytes", dest.getAbsolutePath(), dest.length());

            // 返回相对路径，前端可直接拼接域名进行访问
            String url = "/uploads/images/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
