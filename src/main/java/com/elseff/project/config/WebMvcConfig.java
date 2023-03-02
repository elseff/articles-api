package com.elseff.project.config;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${jwt.secret}")
    String secret;

    @Value("#{'${allowed.origins}'.split(' ')}")
    List<String> origins;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        origins.forEach(origin -> {
            registry.addMapping("/**")
                    .allowedOrigins(origin)
                    .allowedMethods("*");
            log.info("Allowed origin --- " + origin);
        });
    }

}
