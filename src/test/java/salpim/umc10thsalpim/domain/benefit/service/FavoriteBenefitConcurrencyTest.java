package salpim.umc10thsalpim.domain.benefit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import salpim.umc10thsalpim.domain.benefit.entity.WelfareBenefit;
import salpim.umc10thsalpim.domain.benefit.enums.RegionScope;
import salpim.umc10thsalpim.domain.benefit.repository.FavoriteBenefitRepository;
import salpim.umc10thsalpim.domain.benefit.repository.WelfareBenefitRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


//같은 회원이 같은 혜택에 대해 찜 요청을 동시에 보내는 상황을 검증한다.
//
//워커 스레드가 각자 트랜잭션을 열어야 경쟁 조건이 재현되므로
//이 클래스에는 @Transactional 을 붙이지 않는다. 붙이면 준비 데이터가
//커밋되지 않아 워커 스레드에서 조회조차 되지 않는다.

@SpringBootTest
class FavoriteBenefitConcurrencyTest {

    private static final int THREAD_COUNT = 20;

    @Autowired
    private BenefitService benefitService;

    @Autowired
    private FavoriteBenefitRepository favoriteBenefitRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WelfareBenefitRepository welfareBenefitRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long memberId;
    private Long benefitId;
    private Long regionId;

    @BeforeEach
    void setUp() {
        Region region = regionRepository.save(
                Region.create(null, "화전동", RegionLevel.ADMINISTRATIVE_AREA));

        WelfareBenefit benefit = welfareBenefitRepository.save(WelfareBenefit.builder()
                .externalId("benefit-concurrency-1")
                .source("test")
                .title("동시성 테스트용 혜택")
                .easySummary("easy summary")
                .whoCanReceive("who can receive")
                .whatYouReceive("what you receive")
                .recommendedFor("recommended for")
                .regionScope(RegionScope.NONE)
                .build());

        Member member = memberRepository.save(Member.builder()
                .loginType(SocialProvider.LOCAL)
                .phoneNumber("01099998888")
                .password("encoded-password")
                .name("김살핌")
                .birthDate(LocalDate.of(2002, 3, 11))
                .gender(Gender.MALE)
                .roadAddress("경기도 고양시 덕양구 화랑로 28")
                .latitude(BigDecimal.valueOf(37.1234567))
                .longitude(BigDecimal.valueOf(126.1234567))
                .region(region)
                .passwordRecoveryAnswer("가을")
                .welfareCenter(region.getName())
                .build());

        memberId = member.getId();
        benefitId = benefit.getId();
        regionId = region.getId();
    }

// 이 테스트가 만든 행만 지운다. deleteAll() 은 같은 인메모리 DB 를 쓰는
// 다른 테스트의 데이터까지 날리므로 쓰지 않는다.
//
// 파생 삭제 쿼리는 활성 트랜잭션을 요구하는데 이 클래스에는 @Transactional 이
// 없으므로 TransactionTemplate 으로 감싼다. deleteById 는 SimpleJpaRepository 가
// 자체 트랜잭션을 열어주므로 그대로 호출하고, FK 때문에 참조하는 쪽부터 지운다.
    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status ->
                favoriteBenefitRepository.deleteByMemberIdAndBenefitId(memberId, benefitId));
        memberRepository.deleteById(memberId);
        welfareBenefitRepository.deleteById(benefitId);
        regionRepository.deleteById(regionId);
    }

    @Test
    @DisplayName("같은 찜 요청이 동시에 들어와도 모두 성공하고 찜은 하나만 생긴다")
    void concurrentFavoriteRequestsSucceedAndCreateSingleRow() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        List<Throwable> failures = new CopyOnWriteArrayList<>();

        boolean finished;
        boolean terminated;
        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        benefitService.toggleFavoriteBenefit(memberId, benefitId, true);
                    } catch (Throwable t) {
                        failures.add(t);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            finished = doneLatch.await(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
            terminated = executor.awaitTermination(10, TimeUnit.SECONDS);
        }

        assertThat(failures).isEmpty();
        assertThat(favoriteBenefitRepository.countByMemberIdAndBenefitId(memberId, benefitId)).isEqualTo(1);
        assertThat(finished).isTrue();
        assertThat(terminated).isTrue();
    }
}
