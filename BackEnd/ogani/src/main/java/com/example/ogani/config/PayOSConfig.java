package com.example.ogani.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;


@Configuration
public class PayOSConfig implements WebMvcConfigurer {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Bean
    public PayOS payOS() {
        ClientOptions options =
                ClientOptions.builder()
                        .clientId(clientId)
                        .apiKey(apiKey)
                        .checksumKey(checksumKey)
                        .build();
        return new PayOS(options);
    }

    @Bean
    public PayOS payOSPayout() {
        ClientOptions options =
                ClientOptions.builder()
                        .clientId(clientId)
                        .apiKey(apiKey)
                        .checksumKey(checksumKey)
                        .build();
        return new PayOS(options);
    }

}
