package salpim.umc10thsalpim.global.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("integration") //태그 지워야 실행됨
public class BokjiroApiIntegrationTest {

    @Autowired
    BokjiroApiClient bokjiroApiClient;

    @Test
    @DisplayName("복지로 api 연결 확인")
    void bokjiroApiTest(){
        var res = bokjiroApiClient.searchLocalBenefits(1, 10, null, null, "인천", null);
        assertThat(res.getTotalCount()).isPositive();
        assertThat(res.getBenefitList()).isNotEmpty();
        assertThat(res.getBenefitList().get(0).getServId()).isNotBlank();
    }

}
