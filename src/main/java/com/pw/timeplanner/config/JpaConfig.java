package com.pw.timeplanner.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("com.pw.timeplanner.feature")
//@EnableTransactionManagement
public class JpaConfig {

}
