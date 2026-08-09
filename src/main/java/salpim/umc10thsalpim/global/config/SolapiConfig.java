package salpim.umc10thsalpim.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "solapi")
public class SolapiConfig {

    private String apiKey;
    private String apiSecret;
    private String senderNumber;
}
