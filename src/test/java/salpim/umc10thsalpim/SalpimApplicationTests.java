package salpim.umc10thsalpim;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salpim.umc10thsalpim.global.infra.bokjiro.BokjiroApiClient;

@SpringBootTest
class SalpimApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	BokjiroApiClient bokjiroApiClient;

	@Test
	void bokjiroApiTest(){
		var res = bokjiroApiClient.searchBenefits(1, 10, null, "010");
		System.out.println(res.getTotalCount());
		res.getBenefitList().forEach(item -> System.out.println(item.getServId()));
	}

}
