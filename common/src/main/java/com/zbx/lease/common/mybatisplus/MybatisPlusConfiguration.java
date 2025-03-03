package com.zbx.lease.common.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.zbx.lease.web.*.mapper")
public class MybatisPlusConfiguration {

}
