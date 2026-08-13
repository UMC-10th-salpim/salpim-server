package salpim.umc10thsalpim.global.infra;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;
import salpim.umc10thsalpim.global.infra.exception.BokjiroException;
import salpim.umc10thsalpim.global.infra.exception.code.BokjiroErrorCode;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 복지로 API를 실제로 호출하지 않고, 키워드 병렬 호출/병합/부분 실패 처리를 검증한다.
 * 외부 연결 확인은 BokjiroApiIntegrationTest 에서 한다.
 */
class BokjiroApiClientTest {

    private static final String SOURCE_NATIONAL = "National";
    private static final String SOURCE_LOCAL = "Local";
    private static final int PAGE_NO = 1;
    private static final int PAGE_SIZE = 10;

    private final List<URI> nationalRequests = Collections.synchronizedList(new ArrayList<>());
    private final List<URI> localRequests = Collections.synchronizedList(new ArrayList<>());

    @Test
    @DisplayName("검색 키워드가 여러 개면 모든 키워드를 동시에 호출한다")
    void callsEveryKeywordInParallel() {
        List<String> searchWrd = List.of("노인", "돌봄", "의료", "주거", "일자리");
        CountDownLatch allStarted = new CountDownLatch(searchWrd.size());
        AtomicInteger sawEveryKeywordInFlight = new AtomicInteger();

        // 모든 키워드가 동시에 진행 중이어야 래치가 풀린다.
        // 순차 호출이면 마지막 키워드만 래치를 확인할 수 있어 개수가 모자란다.
        BokjiroApiClient client = client(wrd -> Mono.fromCallable(() -> {
            allStarted.countDown();
            if (allStarted.await(2, TimeUnit.SECONDS)) {
                sawEveryKeywordInFlight.incrementAndGet();
            }
            return okResponse(1, wrd);
        }).subscribeOn(Schedulers.boundedElastic()));

        BokjiroApiDTO.BenefitListRes res = client
                .searchBenefitsMono(PAGE_NO, PAGE_SIZE, searchWrd, null, SOURCE_NATIONAL, null, null)
                .block();

        assertThat(sawEveryKeywordInFlight.get())
                .as("키워드는 순차가 아니라 동시에 호출되어야 한다")
                .isEqualTo(searchWrd.size());
        assertThat(searchWrdsOf(nationalRequests)).containsExactlyInAnyOrderElementsOf(searchWrd);
        assertThat(res.getBenefitList()).hasSize(searchWrd.size());
    }

    @Test
    @DisplayName("키워드별 응답을 servId 기준으로 중복 제거해 합친다")
    void mergesResponsesAndRemovesDuplicatedServId() {
        BokjiroApiClient client = client(wrd -> switch (wrd) {
            case "노인" -> ok(10, "S1", "S2");
            case "돌봄" -> ok(10, "S2", "S3");
            default -> ok(0);
        });

        BokjiroApiDTO.BenefitListRes res = client
                .searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인", "돌봄"), null, SOURCE_NATIONAL, null, null)
                .block();

        assertThat(res.getBenefitList())
                .extracting(BokjiroApiDTO.BenefitItem::getServId)
                .containsExactlyInAnyOrder("S1", "S2", "S3");
    }

    @Test
    @DisplayName("전체 건수는 키워드별 응답 중 최댓값을 사용한다")
    void usesMaxTotalCountAcrossKeywords() {
        BokjiroApiClient client = client(wrd -> switch (wrd) {
            case "노인" -> ok(10, "S1");
            case "돌봄" -> ok(70, "S2");
            default -> ok(0);
        });

        BokjiroApiDTO.BenefitListRes res = client
                .searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인", "돌봄"), null, SOURCE_NATIONAL, null, null)
                .block();

        assertThat(res.getMaxTotalCount()).isEqualTo(70);
    }

    @Test
    @DisplayName("일부 키워드 호출이 실패해도 성공한 키워드 결과는 반환한다")
    void ignoresFailedKeywordAndReturnsRest() {
        BokjiroApiClient client = client(wrd -> "실패".equals(wrd) ? serverError() : ok(10, "S1"));

        BokjiroApiDTO.BenefitListRes res = client
                .searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("실패", "성공"), null, SOURCE_NATIONAL, null, null)
                .block();

        assertThat(res.getBenefitList())
                .extracting(BokjiroApiDTO.BenefitItem::getServId)
                .containsExactly("S1");
    }

    @Test
    @DisplayName("모든 키워드 호출이 실패하면 복지로 API 예외가 발생한다")
    void throwsWhenEveryKeywordFails() {
        BokjiroApiClient client = client(wrd -> serverError());

        assertThatThrownBy(() -> client
                .searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인", "돌봄"), null, SOURCE_NATIONAL, null, null)
                .block())
                .isInstanceOf(BokjiroException.class)
                .extracting(e -> ((BokjiroException) e).getErrorCode())
                .isEqualTo(BokjiroErrorCode.BOKJIRO_API_ERROR);
    }

    @Test
    @DisplayName("지원하지 않는 source면 복지로 API 예외가 발생한다")
    void throwsWhenSourceIsNotSupported() {
        BokjiroApiClient client = client(wrd -> ok(10, "S1"));

        assertThatThrownBy(() -> client
                .searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인"), null, "Unknown", null, null)
                .block())
                .isInstanceOf(BokjiroException.class)
                .extracting(e -> ((BokjiroException) e).getErrorCode())
                .isEqualTo(BokjiroErrorCode.BOKJIRO_API_ERROR);

        assertThat(nationalRequests).isEmpty();
        assertThat(localRequests).isEmpty();
    }

    @Test
    @DisplayName("검색 키워드가 없으면 호출 없이 빈 결과를 반환한다")
    void returnsEmptyResultWhenNoKeyword() {
        BokjiroApiClient client = client(wrd -> ok(10, "S1"));

        BokjiroApiDTO.BenefitListRes res = client
                .searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of(), null, SOURCE_NATIONAL, null, null)
                .block();

        assertThat(res.getBenefitList()).isEmpty();
        assertThat(res.getMaxTotalCount()).isZero();
        assertThat(nationalRequests).isEmpty();
    }

    @Test
    @DisplayName("National source는 중앙부처 API만 호출한다")
    void callsOnlyNationalApiForNationalSource() {
        BokjiroApiClient client = client(wrd -> ok(10, "S1"));

        client.searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인"), null, SOURCE_NATIONAL, "인천광역시", "미추홀구")
                .block();

        assertThat(nationalRequests).hasSize(1);
        assertThat(localRequests).isEmpty();
    }

    @Test
    @DisplayName("Local source는 지자체 API만 호출하며 시도/시군구를 전달한다")
    void callsOnlyLocalApiWithRegionForLocalSource() {
        BokjiroApiClient client = client(wrd -> ok(10, "S1"));

        client.searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인"), null, SOURCE_LOCAL, "인천광역시", "미추홀구")
                .block();

        assertThat(nationalRequests).isEmpty();
        assertThat(localRequests).hasSize(1);
        assertThat(queryParam(localRequests.get(0), "ctpvNm")).isEqualTo("인천광역시");
        assertThat(queryParam(localRequests.get(0), "sggNm")).isEqualTo("미추홀구");
    }

    @Test
    @DisplayName("searchBenefitsMono는 구독하기 전까지 API를 호출하지 않는다")
    void doesNotCallApiBeforeSubscribe() {
        BokjiroApiClient client = client(wrd -> ok(10, "S1"));

        Mono<BokjiroApiDTO.BenefitListRes> mono = client
                .searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인"), null, SOURCE_NATIONAL, null, null);

        assertThat(nationalRequests).isEmpty();

        mono.block();

        assertThat(nationalRequests).hasSize(1);
    }

    @Test
    @DisplayName("중앙부처와 지자체를 동시에 조회해도 서로 영향을 주지 않는다")
    void queriesNationalAndLocalTogether() {
        BokjiroApiClient client = client(
                wrd -> ok(10, "N1"),
                wrd -> ok(20, "L1")
        );

        var both = Mono.zip(
                client.searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인"), null, SOURCE_NATIONAL, "인천광역시", "미추홀구"),
                client.searchBenefitsMono(PAGE_NO, PAGE_SIZE, List.of("노인"), null, SOURCE_LOCAL, "인천광역시", "미추홀구")
        ).block();

        assertThat(both.getT1().getBenefitList())
                .extracting(BokjiroApiDTO.BenefitItem::getServId)
                .containsExactly("N1");
        assertThat(both.getT2().getBenefitList())
                .extracting(BokjiroApiDTO.BenefitItem::getServId)
                .containsExactly("L1");
    }

    private BokjiroApiClient client(Function<String, Mono<ClientResponse>> handler) {
        return client(handler, handler);
    }

    private BokjiroApiClient client(Function<String, Mono<ClientResponse>> nationalHandler,
                                    Function<String, Mono<ClientResponse>> localHandler) {
        return new BokjiroApiClient(
                webClient("https://national.bokjiro.test", nationalRequests, nationalHandler),
                "national-service-key",
                webClient("https://local.bokjiro.test", localRequests, localHandler),
                "local-service-key",
                new XmlMapper()
        );
    }

    private WebClient webClient(String baseUrl,
                                List<URI> requests,
                                Function<String, Mono<ClientResponse>> handler) {
        ExchangeFunction exchangeFunction = request -> {
            requests.add(request.url());
            return handler.apply(queryParam(request.url(), "searchWrd"));
        };
        return WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeFunction(exchangeFunction)
                .build();
    }

    private static List<String> searchWrdsOf(List<URI> requests) {
        return requests.stream()
                .map(uri -> queryParam(uri, "searchWrd"))
                .toList();
    }

    private static String queryParam(URI uri, String name) {
        String query = uri.getRawQuery();
        if (query == null) {
            return "";
        }
        for (String param : query.split("&")) {
            if (param.startsWith(name + "=")) {
                return URLDecoder.decode(param.substring(name.length() + 1), StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private static Mono<ClientResponse> ok(int totalCount, String... servIds) {
        return Mono.just(okResponse(totalCount, servIds));
    }

    private static ClientResponse okResponse(int totalCount, String... servIds) {
        StringBuilder xml = new StringBuilder("<wantedList>")
                .append("<resultCode>0</resultCode>")
                .append("<resultMessage>SUCCESS</resultMessage>")
                .append("<totalCount>").append(totalCount).append("</totalCount>");
        for (String servId : servIds) {
            xml.append("<servList>")
                    .append("<servId>").append(servId).append("</servId>")
                    .append("<servNm>").append(servId).append(" 혜택</servNm>")
                    .append("<inqNum>10</inqNum>")
                    .append("</servList>");
        }
        xml.append("</wantedList>");

        return ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, "application/xml;charset=UTF-8")
                .body(xml.toString())
                .build();
    }

    private static Mono<ClientResponse> serverError() {
        return Mono.just(ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR)
                .header(HttpHeaders.CONTENT_TYPE, "application/xml;charset=UTF-8")
                .body("<error/>")
                .build());
    }
}
