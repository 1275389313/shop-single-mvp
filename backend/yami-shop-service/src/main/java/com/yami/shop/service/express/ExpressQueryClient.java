package com.yami.shop.service.express;

import com.yami.shop.bean.app.dto.DeliveryDto;

/**
 * 快递100即时查询。无密钥时不要调用，由 {@link com.yami.shop.service.DeliveryTrackingService} 走 mock。
 */
public interface ExpressQueryClient {

    DeliveryDto query(String companyCode, String trackingNo, String phone);
}
