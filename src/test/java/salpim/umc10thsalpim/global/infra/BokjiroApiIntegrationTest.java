package salpim.umc10thsalpim.global.infra;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
//@Disabled("외부 API 및 환경 변수 필요")
public class BokjiroApiIntegrationTest {

    @Autowired
    BokjiroApiClient bokjiroApiClient;

    @Test
    @DisplayName("중앙부처 복지로 api 연결 확인")
    void nationalBokjiroApiTest(){
        var res = bokjiroApiClient.searchNationalBenefits(1, 10, null, null);
        assertThat(res.getTotalCount()).isPositive();
        assertThat(res.getBenefitList()).isNotEmpty();
        assertThat(res.getBenefitList().get(0).getServId()).isNotBlank();
    }

    @Test
    @DisplayName("지자체 복지로 api 연결 확인")
    void localBokjiroApiTest(){
        var res = bokjiroApiClient.searchLocalBenefits(1, 10, null, null, "인천", null);
        assertThat(res.getTotalCount()).isPositive();
        assertThat(res.getBenefitList()).isNotEmpty();
        assertThat(res.getBenefitList().get(0).getServId()).isNotBlank();
    }

}
