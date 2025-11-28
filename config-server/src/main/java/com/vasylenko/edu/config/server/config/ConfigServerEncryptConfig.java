package com.vasylenko.edu.config.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
public class ConfigServerEncryptConfig {
    @Bean
    @Primary
    public TextEncryptor textEncryptor() {
        return new TextEncryptor() {
            @Override
            public String encrypt(String text) {
                return text;
            }

            @Override
            public String decrypt(String encryptedText) {
                return encryptedText;
            }
        };
    }
}
