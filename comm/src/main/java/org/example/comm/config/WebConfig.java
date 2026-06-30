package org.example.comm.config;

import org.example.comm.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                // 要拦截的接口：发布商品必须登录
                .addPathPatterns("/product/publish")
                // 放行的接口（不用登录就能访问）
                .excludePathPatterns(
                        "/user/login",       // 登录接口
                        "/user/hello",       // 测试接口
                        "/user/test/**",     // 所有测试接口
                        "/product/list",     // 查询商品列表
                        "/product/*",        // 查询单个商品
                        "/uploads/**"        // 静态资源（图片）
                );
    }
}