package com.gikk;

import javax.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "bot")
public class SystemConfig {

    @NotBlank
    public String password;
    @NotBlank
    public String channel;
    @NotBlank
    public String username;
    @NotBlank
    public String currency;

    public String getPassword() {
        return password;
    }

    public String getChannel() {
        return channel;
    }

    public String getUsername() {
        return username;
    }

    public String getCurrency() {
        return currency;
    }
}
