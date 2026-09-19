package com.yami.shop.api.controller;

import com.yami.shop.bean.app.dto.FileUploadDto;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.ImgUploadUtil;
import com.yami.shop.service.AttachFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Buyer image upload for 评价晒图. Local disk by default (uploadType=1); no COS/Qiniu keys required.
 */
@RestController
@RequestMapping("/p/file")
@Tag(name = "用户端文件上传")
@AllArgsConstructor
public class FileController {

    private static final String PLACEHOLDER_PATH = "placeholder/review.svg";
    private static final String PLACEHOLDER_SVG = """
            <svg xmlns="http://www.w3.org/2000/svg" width="400" height="400">
              <rect fill="#f2f2f2" width="400" height="400"/>
              <text x="50%" y="50%" text-anchor="middle" dy=".3em" fill="#999" font-size="28">晒图占位</text>
            </svg>
            """;

    private final AttachFileService attachFileService;
    private final ImgUploadUtil imgUploadUtil;

    @PostMapping("/upload")
    @Operation(summary = "上传评价图片，返回相对路径与可访问 URL")
    public ServerResponseEntity<FileUploadDto> upload(@RequestParam("file") MultipartFile file,
                                                      HttpServletRequest request) throws IOException {
        if (file == null || file.isEmpty()) {
            return ServerResponseEntity.success(placeholder(request));
        }
        String fileName = attachFileService.uploadFile(file);
        FileUploadDto dto = new FileUploadDto();
        dto.setFilePath(fileName);
        dto.setUrl(toUrl(fileName, request));
        dto.setMock(false);
        return ServerResponseEntity.success(dto);
    }

    @PostMapping("/placeholder")
    @Operation(summary = "不传文件时的本地占位图（无需 COS 密钥）")
    public ServerResponseEntity<FileUploadDto> placeholderApi(HttpServletRequest request) throws IOException {
        return ServerResponseEntity.success(placeholder(request));
    }

    private FileUploadDto placeholder(HttpServletRequest request) throws IOException {
        File dest = new File(imgUploadUtil.getUploadPath(), PLACEHOLDER_PATH);
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("cannot create " + parent);
        }
        if (!dest.exists()) {
            Files.writeString(dest.toPath(), PLACEHOLDER_SVG, StandardCharsets.UTF_8);
        }
        FileUploadDto dto = new FileUploadDto();
        dto.setFilePath(PLACEHOLDER_PATH);
        dto.setUrl(toUrl(PLACEHOLDER_PATH, request));
        dto.setMock(true);
        return dto;
    }

    private String toUrl(String fileName, HttpServletRequest request) {
        String resourceUrl = imgUploadUtil.getResourceUrl();
        if (resourceUrl != null && !resourceUrl.isBlank()) {
            return resourceUrl.endsWith("/") ? resourceUrl + fileName : resourceUrl + "/" + fileName;
        }
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + "/mall4j/img/" + fileName;
    }
}
