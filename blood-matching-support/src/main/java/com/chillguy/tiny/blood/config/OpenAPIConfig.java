package com.chillguy.tiny.blood.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Blood Matching API",
                version = "1.0",
                description = "API hỗ trợ matching và quản lý đơn xin máu"
        )
)
public class OpenAPIConfig {
}
