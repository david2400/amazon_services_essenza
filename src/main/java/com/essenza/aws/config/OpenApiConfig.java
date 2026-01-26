package com.essenza.aws.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@OpenAPIDefinition(
        info = @Info(
                title = "Essenza Tienda API",
                version = "v1"
        ),
        servers = {
                @Server(url = "/", description = "Default Server")
        }
)
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi amazonS3Api() {
        return GroupedOpenApi.builder()
                .group("amazon-s3")
                .displayName("Amazon S3")
                .packagesToScan("com.essenza.aws.modules.amazonS3")
                .pathsToMatch(
                        "/api/aws/files-amazon/**",
                        "/api/aws/document/**",
                        "/api/aws/document_bucket_aws/**",
                        "/api/aws/buket_aws/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi amazonSnsApi() {
        return GroupedOpenApi.builder()
                .group("amazon-sns")
                .displayName("Amazon SNS")
                .packagesToScan("com.essenza.aws.modules.amazonSNS")
                .pathsToMatch("/api/aws/sns/**")
                .build();
    }

    @Controller
    static class SwaggerUiRedirectController {

        @GetMapping(value = {"/", ""})
        public String redirectRoot() {
            return "redirect:/swagger-ui/index.html";
        }

        @GetMapping(value = {"/swagger-ui", "/swagger-ui/"})
        public String redirectSwaggerUi() {
            return "redirect:/swagger-ui/index.html";
        }
    }
}
