package com.yami.shop.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Read tz_sys_config without depending on yami-shop-sys (admin-only module).
 */
@Mapper
public interface SysParamMapper {

    @Select("SELECT param_value FROM tz_sys_config WHERE param_key = #{key} LIMIT 1")
    String getValue(@Param("key") String key);
}
