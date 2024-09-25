package org.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class BcryptBean {
    @Bean
    public BCryptPasswordEncoder brcryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
