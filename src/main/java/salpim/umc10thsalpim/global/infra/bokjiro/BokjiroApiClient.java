package salpim.umc10thsalpim.global.infra.bokjiro;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;
import salpim.umc10thsalpim.global.infra.exception.BokjiroException;
import salpim.umc10thsalpim.global.infra.exception.code.BokjiroErrorCode;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

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

    private Mono<BokjiroApiDTO.BenefitListRes> searchNationalBenefits(int pageNo, int pageSize, String searchWrd, String intrsThemaArray) {
        return nationalWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/NationalWelfarelistV001")
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
                .map(this::parseAndValidate);
    }

    public Mono<BokjiroApiDTO.BenefitListRes> searchLocalBenefits(int pageNo, int pageSize, String searchWrd, String intrsThemaArray, String ctpvNm, String sggNm){
        return localWebClient.get()
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
                .map(this::parseAndValidate);
    }

    private BokjiroApiDTO.BenefitListRes parseAndValidate(String xml){

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

    public BokjiroApiDTO.BenefitListRes searchBenefits(int pageNo, int pageSize, List<String> searchWrd, String intrsThemaArray, String source, String ctpvNm, String sggNm){

        List<BokjiroApiDTO.BenefitListRes> results = List.of();

        if (source.equals("National")) {

            results = Flux.fromIterable(searchWrd)
                    .flatMap(wrd -> searchNationalBenefits(pageNo, pageSize, wrd, intrsThemaArray).onErrorResume(e->Mono.empty()), 3)
                    .collectList()
                    .block();


        } else if (source.equals("Local")) {

            results = Flux.fromIterable(searchWrd)
                    .flatMap(wrd -> searchLocalBenefits(pageNo, pageSize, wrd, intrsThemaArray, ctpvNm, sggNm).onErrorResume(e->Mono.empty()), 3)
                    .collectList()
                    .block();

        }

        return mergeRes(results);
    }

    //중복 제거 및 합치기
    private BokjiroApiDTO.BenefitListRes mergeRes(List<BokjiroApiDTO.BenefitListRes> results) {

        List<BokjiroApiDTO.BenefitItem> benefitList =
                results.stream()
                        .filter(Objects::nonNull)
                        .flatMap(res -> res.getBenefitList().stream())
                        .filter(item -> item.getServId()!=null)
                        .collect(Collectors.toMap(
                                BokjiroApiDTO.BenefitItem::getServId,
                                Function.identity(),
                                (a, b)->a,
                                LinkedHashMap::new
                        ))
                        .values()
                        .stream()
                        .toList();

        int totalCount = results.stream()
                .filter(Objects::nonNull)
                .mapToInt(BokjiroApiDTO.BenefitListRes::getMaxTotalCount)
                .max()
                .orElse(0);

        String resultMessage = null;
        String resultCode = null;

        return BokjiroApiDTO.BenefitListRes.builder()
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .benefitList(benefitList)
                .maxTotalCount(totalCount)
                .build();
    }

}
