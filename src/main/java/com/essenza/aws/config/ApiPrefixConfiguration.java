package com.essenza.aws.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.List;

@Configuration
public class ApiPrefixConfiguration implements WebMvcRegistrations {

    private static final String API_PREFIX = "/api/aws";
    private static final String BASE_PACKAGE = "com.essenza.aws";
    private static final List<String> PREFIX_EXCLUDED_PACKAGES = List.of(
            "org.springdoc",
            "org.springframework.boot.actuate",
            "com.essenza.aws.config"
    );

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandlerMapping() {
            @Override
            protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
                if (!shouldApplyPrefix(method.getDeclaringClass().getPackageName())) {
                    super.registerHandlerMethod(handler, method, mapping);
                    return;
                }

                var newMapping = RequestMappingInfo
                        .paths(API_PREFIX)
                        .build()
                        .combine(mapping);
                super.registerHandlerMethod(handler, method, newMapping);
            }

            private boolean shouldApplyPrefix(String packageName) {
                return packageName != null
                        && packageName.startsWith(BASE_PACKAGE)
                        && PREFIX_EXCLUDED_PACKAGES.stream().noneMatch(packageName::startsWith);
            }
        };
    }
}
