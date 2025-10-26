package com.essenza.aws.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

@Configuration
public class ApiPrefixConfiguration implements WebMvcRegistrations {

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {
            @Override
            protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
                // Añade prefijo si no está presente
                String prefix = "/api/aws";
                var newMapping = RequestMappingInfo
                        .paths(prefix)
                        .build()
                        .combine(mapping);
                super.registerHandlerMethod(handler, method, newMapping);
            }
        };
    }
}
