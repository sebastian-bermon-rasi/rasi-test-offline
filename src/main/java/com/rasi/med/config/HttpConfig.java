package com.rasi.med.config;

import org.springframework.context.annotation.*;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpConfig {
    @Bean
    public RestTemplate restTemplate(){ return new RestTemplate(); }
}
