package com.mutil.sso.config;

import com.mutil.sso.intercepor.SSOIntercepor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfigurer implements WebMvcConfigurer {

    @Autowired
    private SSOIntercepor intercepor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(intercepor)
                .addPathPatterns("/**")
                .excludePathPatterns("/login/**");
    }


}
