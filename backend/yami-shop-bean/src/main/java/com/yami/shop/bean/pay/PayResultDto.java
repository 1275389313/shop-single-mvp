package com.yami.shop.bean.pay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Pay API result. Uni-app treats a non-empty object as success.
 */
@Data
public class PayResultDto {

    @Schema(description = "Whether the order is already marked paid")
    private boolean paid;

    @Schema(description = "Internal pay serial")
    private String payNo;

    @Schema(description = "True when mock pay path was used")
    private boolean mock;
}
