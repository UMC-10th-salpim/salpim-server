package salpim.umc10thsalpim.global.infra.bokjiro;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import salpim.umc10thsalpim.global.apiPayload.exception.code.BokjiroErrorCode;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;
import salpim.umc10thsalpim.global.infra.exception.BokjiroException;

@Component
public class BokjiroApiClient {

    private final WebClient webClient;
    private final String serviceKey;
    private final XmlMapper xmlMapper = new XmlMapper();

    public BokjiroApiClient(@Value("${bokjiro.base-url}") String baseUrl,
                            @Value("${bokjiro.service-key}") String serviceKey) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.serviceKey = serviceKey;
    }

    public BokjiroApiDTO.BenefitListRes searchBenefits(int pageNo, int pageSize, String searchWrd, String intrsThemaArray){
        String xml =  webClient.get()
                .uri(uriBuilder ->uriBuilder.path("/NationalWelfarelistV001")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("callTp", "L")
                        .queryParam("pageNo", pageNo)
                        .queryParam("numOfRows", pageSize)
                        .queryParam("srchKeyCode", "003")
                        .queryParam("searchWrd", searchWrd)
                        .queryParam("lifeArray", "006")
                        .queryParam("intrsThemaArray", intrsThemaArray)
                        .queryParam("orderBy", "popular")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try{
            BokjiroApiDTO.BenefitListRes res =
                    xmlMapper.readValue(xml, BokjiroApiDTO.BenefitListRes.class);

            if (!"0".equals(res.getResultCode())) {
                throw new BokjiroException(BokjiroErrorCode.BOKJIRO_API_ERROR);
            }
            return res;
        }catch(Exception e){
            throw new BokjiroException(BokjiroErrorCode.BOKJIRO_API_ERROR);
        }

    }

}
