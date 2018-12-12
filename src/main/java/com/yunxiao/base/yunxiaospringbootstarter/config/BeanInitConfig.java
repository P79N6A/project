package com.yunxiao.base.yunxiaospringbootstarter.config;

import com.yunxiao.base.yunxiaospringbootstarter.service.CASLoginHandle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author voidman
 * @date 2018/05/14
 */
@Configuration
public class BeanInitConfig {
    /**
     * 登录handel的bean实例化
     * @return
     */
    @Bean(name = "casLoginHandle")
    public CASLoginHandle getCASLoginHandle() {
        return new CASLoginHandle();
    }
}
