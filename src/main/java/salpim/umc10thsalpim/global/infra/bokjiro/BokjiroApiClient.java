package salpim.umc10thsalpim.global.infra.bokjiro;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import salpim.umc10thsalpim.global.infra.exception.code.BokjiroErrorCode;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;
import salpim.umc10thsalpim.global.infra.exception.BokjiroException;

import java.time.Duration;

@Component
public class BokjiroApiClient {

    private final WebClient nationalWebClient;
    private final WebClient localWebClient;
    private final String nationalServiceKey;
    private final String localServiceKey;
    private final XmlMapper xmlMapper;

    public BokjiroApiClient(@Qualifier("bokjiroNationalWebClient") WebClient nationalWebClient,
                            @Value("${bokjiro.service-key}") String serviceKey,
                            @Qualifier("bokjiroLocalWebClient") WebClient localWebClient,
                            @Value("${bokjiro.local-service-key}") String localServiceKey,
                            XmlMapper xmlMapper ) {
        this.nationalServiceKey = serviceKey;
        this.localServiceKey = localServiceKey;
        this.nationalWebClient = nationalWebClient;
        this.localWebClient = localWebClient;
        this.xmlMapper = xmlMapper;
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

            if ("40".equals(res.getResultCode())) {
                return res;
            }

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

            if ("40".equals(res.getResultCode())) {
                return res;
            }

            if (!"0".equals(res.getResultCode())) {
                throw new BokjiroException(BokjiroErrorCode.BOKJIRO_API_ERROR);
            }
            return res;
        }catch(Exception e){
            throw new BokjiroException(BokjiroErrorCode.BOKJIRO_API_ERROR);
        }

    }

}
