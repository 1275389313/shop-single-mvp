package com.yami.shop.bean.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Local / OSS upload result. {@code filePath} is stored in {@code tz_prod_comm.pics}.
 */
@Data
@Schema(description = "文件上传结果")
public class FileUploadDto {

    @Schema(description = "相对路径，写入评价 pics")
    private String filePath;

    @Schema(description = "可直接展示的 URL")
    private String url;

    @Schema(description = "true 表示占位图，未走七牛/COS")
    private Boolean mock;
}
