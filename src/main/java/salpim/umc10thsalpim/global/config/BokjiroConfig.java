package salpim.umc10thsalpim.global.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BokjiroConfig {

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

    @Bean
    public WebClient bokjiroNationalWebClient(@Value("${bokjiro.base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }

    @Bean
    public WebClient bokjiroLocalWebClient(@Value("${bokjiro.local-base-url}") String baseUrl) {
        return WebClient.builder().baseUrl(baseUrl).build();
    }
}
