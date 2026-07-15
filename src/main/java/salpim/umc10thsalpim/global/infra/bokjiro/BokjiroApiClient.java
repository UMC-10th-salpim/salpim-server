package salpim.umc10thsalpim.global.infra.bokjiro;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class BokjiroApiClient {

    private final WebClient webClient;
    private final String serviceKey;

    public BokjiroApiClient(@Value("${bokjiro.base-url}") String baseUrl,
                            @Value("${bokjiro.service-key}") String serviceKey) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
    }

    public String fetchList(int pageNo, int pageSize, String searchWrd, String intrsThemaArray){
        return  webClient.get()
                .uri(uriBuilder ->uriBuilder.path("/NationalWelfarelistV001")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("callTp", "L")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", pageSize)
                        .queryParam("srchKeyCode", "003")
                        .queryParam("searchWrd", searchWrd)
                        .queryParam("lifeArray", "006")
                        .queryParam("intrsThemaArray", intrsThemaArray)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

}
