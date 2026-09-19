package com.yami.shop.common.util;

import cn.hutool.core.util.StrUtil;
import com.yami.shop.common.bean.ImgUpload;
import com.yami.shop.common.exception.YamiShopBindException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 本地文件上传与删除
 * @author TRACK
 */
@Component
public class ImgUploadUtil {

    @Autowired
    private ImgUpload imgUpload;

    public Integer getUploadType() {
        Integer uploadType = imgUpload.getUploadType();
        if (Objects.isNull(uploadType)) {
            throw new YamiShopBindException("请配置图片存储方式");
        }
        return uploadType;
    }

    public String getUploadPath() {
        String imagePath = imgUpload.getImagePath();
        if (Objects.isNull(imagePath) || StrUtil.isBlank(imagePath)) {
            throw new YamiShopBindException("请配置图片存储路径");
        }
        return imagePath;
    }

    public String getResourceUrl() {
        String resourceUrl = imgUpload.getResourceUrl();
        if (Objects.isNull(resourceUrl) || StrUtil.isBlank(resourceUrl)) {
            throw new YamiShopBindException("请配置图片路径");
        }
        return resourceUrl;
    }

    public String upload(MultipartFile img, String fileName) {
        String filePath = imgUpload.getImagePath();
        File dest = new File(filePath, fileName).getAbsoluteFile();
        File parent = dest.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean result = parent.mkdirs();
            if (!result && !parent.exists()) {
                throw new YamiShopBindException("创建目录：" + parent.getAbsolutePath() + "失败");
            }
        }
        if (dest.exists() && dest.isDirectory()) {
            // leftover of the old mkdirs-on-filename bug
            boolean deleted = dest.delete();
            if (!deleted) {
                throw new YamiShopBindException("图片上传失败");
            }
        }
        try {
            img.transferTo(dest);
        } catch (IOException e) {
            throw new YamiShopBindException("图片上传失败");
        }
        return fileName;
    }

    public void delete(String fileName) {
        String filePath = imgUpload.getImagePath();
        File file = new File(filePath + fileName);
        file.deleteOnExit();
    }
}
