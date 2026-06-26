package com.example.yupicturebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GlobalCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")              // 所有接口
                .allowedOriginPatterns("*")            // 允许所有来源（前端域名）
                .allowedMethods("*")            // GET, POST, PUT, DELETE...
                .allowedHeaders("*")            // 允许所有请求头
                .allowCredentials(true)  ;      // 是否允许携带 cookie
    }
}
