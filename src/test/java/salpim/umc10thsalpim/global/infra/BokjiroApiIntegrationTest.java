package salpim.umc10thsalpim.global.infra;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;
import salpim.umc10thsalpim.global.infra.dto.BokjiroApiDTO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled("외부 API 및 환경 변수 필요")
public class BokjiroApiIntegrationTest {

    private static final List<String> SEARCH_KEYWORDS = List.of("노인", "돌봄", "의료");

    @Autowired
    BokjiroApiClient bokjiroApiClient;

    @Test
    @DisplayName("중앙부처 복지로 api 연결 확인")
    void nationalBokjiroApiTest(){
        var res = bokjiroApiClient
                .searchBenefitsMono(1, 10, List.of(""), null, "National", null, null)
                .block();
        assertThat(res.getMaxTotalCount()).isPositive();
        assertThat(res.getBenefitList()).isNotEmpty();
        assertThat(res.getBenefitList().get(0).getServId()).isNotBlank();
    }

    @Test
    @DisplayName("지자체 복지로 api 연결 확인")
    void localBokjiroApiTest(){
        var res = bokjiroApiClient
                .searchBenefitsMono(1, 10, List.of(""), null, "Local", "인천", null)
                .block();
        assertThat(res.getMaxTotalCount()).isPositive();
        assertThat(res.getBenefitList()).isNotEmpty();
        assertThat(res.getBenefitList().get(0).getServId()).isNotBlank();
    }

    @Test
    @DisplayName("중앙부처 여러 키워드 병렬 조회 확인")
    void nationalBokjiroApiParallelKeywordTest(){
        var res = bokjiroApiClient
                .searchBenefitsMono(1, 10, SEARCH_KEYWORDS, null, "National", null, null)
                .block();

        assertThat(res).isNotNull();
        assertThat(res.getMaxTotalCount()).isPositive();
        assertThat(res.getBenefitList()).isNotEmpty();
        assertThat(res.getBenefitList())
                .extracting(BokjiroApiDTO.BenefitItem::getServId)
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("지자체 여러 키워드 병렬 조회 확인")
    void localBokjiroApiParallelKeywordTest(){
        var res = bokjiroApiClient
                .searchBenefitsMono(1, 10, SEARCH_KEYWORDS, null, "Local", "인천", null)
                .block();

        assertThat(res).isNotNull();
        assertThat(res.getMaxTotalCount()).isPositive();
        assertThat(res.getBenefitList()).isNotEmpty();
        assertThat(res.getBenefitList())
                .extracting(BokjiroApiDTO.BenefitItem::getServId)
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("중앙부처와 지자체 동시 조회 확인")
    void nationalAndLocalBokjiroApiConcurrentTest(){
        var both = Mono.zip(
                bokjiroApiClient.searchBenefitsMono(1, 10, SEARCH_KEYWORDS, null, "National", "인천", null),
                bokjiroApiClient.searchBenefitsMono(1, 10, SEARCH_KEYWORDS, null, "Local", "인천", null)
        ).block();

        assertThat(both).isNotNull();
        assertThat(both.getT1().getBenefitList()).isNotEmpty();
        assertThat(both.getT2().getBenefitList()).isNotEmpty();
    }

}
