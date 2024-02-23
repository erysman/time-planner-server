package com.pw.timeplanner.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
class ValidationConfig {

    @Bean
    Validator defaultValidator() {
        return new LocalValidatorFactoryBean();
    }
}
