package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.easypost.exception.General.MissingParameterError;
import com.easypost.service.EasyPostClient;

@Configuration
public class EasyPostConfig {

    @Bean
    public EasyPostClient easyPostClient(@Value("${easypost.api.key}") String apiKey) throws MissingParameterError {
        return new EasyPostClient(apiKey);
    }
}
