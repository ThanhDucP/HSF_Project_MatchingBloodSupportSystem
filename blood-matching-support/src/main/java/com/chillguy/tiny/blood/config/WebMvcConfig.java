package com.chillguy.tiny.blood.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        // Map URLs directly to templates without controller
        registry.addViewController("/").setViewName("index");
        registry.addViewController("/home").setViewName("index");
        registry.addViewController("/index").setViewName("index");
        
        registry.addViewController("/auth/login").setViewName("auth/login");
        registry.addViewController("/auth/register").setViewName("auth/register");
        
        // Map blood request pages directly to templates
        registry.addViewController("/blood-requests").setViewName("blood-request/list");
        registry.addViewController("/blood-requests/my").setViewName("blood-request/my-requests");
        registry.addViewController("/blood-requests/confirm-by-token").setViewName("confirm-request");
        registry.addViewController("/blood-requests/create").setViewName("blood-request/create");
        registry.addViewController("/profile").setViewName("profile");
    }
}