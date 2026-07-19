package salpim.umc10thsalpim.global.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WelfareApiConfig {

   // 복지서비스(지자체/중앙) api용 RestClient
   // baseUrl은 각 서비스에서 application.yml 값을 주입받음
   @Bean
   public RestClient welfareRestClient(RestClient.Builder restClientBuilder) {
      return restClientBuilder.build();
   }

   // RestClient.Builder 직접 등록
   @Bean
   public RestClient.Builder restClientBuilder() {
      return RestClient.builder();
   }
}
