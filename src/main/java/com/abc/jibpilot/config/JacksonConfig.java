package com.abc.jibpilot.config;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    /**
     * Provides JsonMapper bean for Spring Boot 4 compatibility.
     * JsonMapper is the recommended type-safe alternative to ObjectMapper.
     * This bean uses Jackson's default configuration which is compatible
     * with Spring Boot's JSON handling.
     */
    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .build();
    }
}

