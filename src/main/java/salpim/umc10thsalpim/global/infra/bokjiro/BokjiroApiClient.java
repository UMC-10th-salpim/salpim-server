package salpim.umc10thsalpim.global.infra.bokjiro;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import salpim.umc10thsalpim.global.apiPayload.exception.code.BokjiroErrorCode;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;
import salpim.umc10thsalpim.global.infra.exception.BokjiroException;

import java.time.Duration;

@Component
public class BokjiroApiClient {

    private final WebClient nationalWebClient;
    private final WebClient localWebClient;
    private final String nationalServiceKey;
    private final String localServiceKey;
    private final XmlMapper xmlMapper = new XmlMapper();

    public BokjiroApiClient(@Value("${bokjiro.base-url}") String baseUrl,
                            @Value("${bokjiro.service-key}") String serviceKey,
                            @Value("${bokjiro.local-base-url}") String localBaseUrl,
                            @Value("${bokjiro.local-service-key}") String localServiceKey) {
        this.nationalWebClient = WebClient.builder().baseUrl(baseUrl).build();
        this.nationalServiceKey = serviceKey;
        this.localWebClient = WebClient.builder().baseUrl(localBaseUrl).build();
        this.localServiceKey = localServiceKey;
    }

    public BokjiroApiDTO.BenefitListRes searchNationalBenefits(int pageNo, int pageSize, String searchWrd, String intrsThemaArray){
        String xml;
        try{
            xml =  nationalWebClient.get()
                    .uri(uriBuilder ->uriBuilder.path("/NationalWelfarelistV001")
                            .queryParam("serviceKey", nationalServiceKey)
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
                    .timeout(Duration.ofSeconds(60))
                    .block();
        } catch (Exception e){
            throw new BokjiroException(BokjiroErrorCode.BOKJIRO_TIME_OUT);
        }


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

    public BokjiroApiDTO.BenefitListRes searchLocalBenefits(int pageNo, int pageSize, String searchWrd, String intrsThemaArray, String ctpvNm, String sggNm){
        String xml;
        try{
            xml =  localWebClient.get()
                    .uri(uriBuilder ->uriBuilder.path("/LcgvWelfarelist")
                            .queryParam("serviceKey", localServiceKey)
                            .queryParam("pageNo", pageNo)
                            .queryParam("numOfRows", pageSize)
                            .queryParam("searchWrd", searchWrd)
                            .queryParam("lifeArray", "006")
                            .queryParam("intrsThemaArray", intrsThemaArray)
                            .queryParam("arrgOrd", "002")
                            .queryParam("ctpvNm", ctpvNm)
                            .queryParam("sggNm", sggNm)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
        } catch(Exception e){
            throw new BokjiroException(BokjiroErrorCode.BOKJIRO_TIME_OUT);
        }

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
