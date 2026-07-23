package salpim.umc10thsalpim.domain.map.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import salpim.umc10thsalpim.domain.map.dto.ExternalWelfareResponse;

import java.time.Duration;

@Slf4j
@Service
public class WelfareApiClient {

    private final WebClient nationalWebClient;
    private final WebClient localWebClient;
    private final XmlMapper xmlMapper;

    private final String centralServiceKey;
    private final String localServiceKey;

    public WelfareApiClient(
            @Qualifier("bokjiroNationalWebClient") WebClient nationalWebClient,
            @Qualifier("bokjiroLocalWebClient") WebClient localWebClient,
            XmlMapper xmlMapper,
            @Value("${welfare.api.CentralKey}") String centralServiceKey,
            @Value("${welfare.api.LocalKey}") String localServiceKey
    ) {
        this.nationalWebClient = nationalWebClient;
        this.localWebClient = localWebClient;
        this.xmlMapper = xmlMapper;
        this.centralServiceKey = centralServiceKey;
        this.localServiceKey = localServiceKey;
    }

    // 1. 중앙 복지 혜택 조회
    public ExternalWelfareResponse fetchRawCentralBenefits() {
        String xmlResponse;
        try {
            xmlResponse = nationalWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/NationalWelfarelistV001")
                            .queryParam("serviceKey", centralServiceKey)
                            .queryParam("callTp", "L")
                            .queryParam("pageNo", "1")
                            .queryParam("numOfRows", "30")
                            .queryParam("srchKeyCode", "003")
                            .queryParam("searchWrd", "")
                            .queryParam("lifeArray", "006")
                            .queryParam("orderBy", "popular")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();
        } catch (Exception e) {
            log.error("[중앙 복지 API] HTTP 에러 발생: {}", e.getMessage());
            return new ExternalWelfareResponse();
        }

        return parseXmlToResponse(xmlResponse, "중앙");
    }

    // 2. 지자체 복지 혜택 조회
    public ExternalWelfareResponse fetchRawLocalBenefits(String sido, String sigungu) {
        if (sido == null || sigungu == null) return new ExternalWelfareResponse();

        String xmlResponse;
        try {
            xmlResponse = localWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/LcgvWelfarelist")
                            .queryParam("serviceKey", localServiceKey)
                            .queryParam("pageNo", "1")
                            .queryParam("numOfRows", "30")
                            .queryParam("lifeArray", "006")
                            .queryParam("arrgOrd", "002")
                            .queryParam("ctpvNm", sido)
                            .queryParam("sggNm", sigungu)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))) //타임아웃이나 네트워크 에러 발생 시, 2초 간격으로 최대 2번까지 자동 재시도
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info(" 지자체 API 원본 응답 XML: \n{}", xmlResponse);
        } catch (Exception e) {
            log.error(" [지자체 복지 API] HTTP 에러 발생: {}", e.getMessage());
            return new ExternalWelfareResponse();
        }

        return parseXmlToResponse(xmlResponse, "지자체");
    }

    private ExternalWelfareResponse parseXmlToResponse(String xml, String apiType) {
        if (xml == null || xml.isBlank()) {
            return new ExternalWelfareResponse();
        }

        try {
            //XmlMapper를 사용하여 수동으로 객체 변환
            ExternalWelfareResponse response = xmlMapper.readValue(xml, ExternalWelfareResponse.class);

            // 공공데이터 API에서 에러 코드(resultCode)를 보냈는지 체크 (0이나 00이 정상이므로, 이외에는 로그 출력)
            if (response.getResultCode() != null && !response.getResultCode().contains("0")) {
                log.warn("⚠️ [{}] API 서버 응답 코드 이상: {}", apiType, response.getResultCode());
            }

            return response;
        } catch (Exception e) {
            log.error("🚨 [{}] XML 파싱 에러! 수신된 실제 XML:\n{}", apiType, xml, e);
            return new ExternalWelfareResponse();
        }
    }
}
