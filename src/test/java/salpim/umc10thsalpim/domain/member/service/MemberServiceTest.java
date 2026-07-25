package salpim.umc10thsalpim.domain.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.service.PhoneVerificationService;
import salpim.umc10thsalpim.domain.member.dto.MemberReqDTO;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long SIDO_ID = 1L;
    private static final Long SIGUNGU_ID = 2L;
    private static final Long DONG_ID = 3L;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private PhoneVerificationService phoneVerificationService;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원의 동 지역을 기준으로 마이페이지 정보를 조회한다")
    void getMyPageSuccess() {
        Member member = createMember(DONG_ID);

        Region dong = createRegion(DONG_ID, SIGUNGU_ID, "용현동", RegionLevel.DONG);
        Region sigungu = createRegion(SIGUNGU_ID, SIDO_ID, "미추홀구", RegionLevel.SIGUNGU);
        Region sido = createRegion(SIDO_ID, null, "인천광역시", RegionLevel.SIDO);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(DONG_ID)).willReturn(Optional.of(dong));
        given(regionRepository.findById(SIGUNGU_ID)).willReturn(Optional.of(sigungu));
        given(regionRepository.findById(SIDO_ID)).willReturn(Optional.of(sido));

        MemberResDTO.MyPageInfo result = memberService.getMyPage(MEMBER_ID);

        assertThat(result).isEqualTo(
                new MemberResDTO.MyPageInfo("홍길동", "인천광역시", "미추홀구")
        );
    }

    @Test
    @DisplayName("존재하지 않는 회원의 마이페이지를 조회하면 예외가 발생한다")
    void throwsExceptionWhenMemberIsNotFound() {
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

        MemberException exception = assertThrows(
                MemberException.class,
                () -> memberService.getMyPage(MEMBER_ID)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원의 지역 정보가 설정되지 않으면 예외가 발생한다")
    void throwsExceptionWhenMemberRegionIsNotSet() {
        Member member = createMember(null);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

        MemberException exception = assertThrows(
                MemberException.class,
                () -> memberService.getMyPage(MEMBER_ID)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.MEMBER_REGION_NOT_SET);
    }

    @Test
    @DisplayName("회원이 설정한 지역을 찾을 수 없으면 예외가 발생한다")
    void throwsExceptionWhenMemberRegionIsNotFound() {
        Member member = createMember(DONG_ID);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(DONG_ID)).willReturn(Optional.empty());

        RegionException exception = assertThrows(
                RegionException.class,
                () -> memberService.getMyPage(MEMBER_ID)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(RegionErrorCode.REGION_NOT_FOUND);
    }

    @Test
    @DisplayName("지역 계층의 부모 정보가 없으면 예외가 발생한다")
    void throwsExceptionWhenRegionHierarchyIsInvalid() {
        Member member = createMember(DONG_ID);
        Region dong = createRegion(DONG_ID, null, "용현동", RegionLevel.DONG);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(DONG_ID)).willReturn(Optional.of(dong));

        RegionException exception = assertThrows(
                RegionException.class,
                () -> memberService.getMyPage(MEMBER_ID)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(RegionErrorCode.REGION_HIERARCHY_INVALID);
    }

    @Test
    @DisplayName("동 단위 지역으로 회원 개인정보를 수정한다")
    void updateProfileSuccess() {
        Member member = createMember(DONG_ID);
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.DONG);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(10L);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(10L)).willReturn(Optional.of(newDong));

        memberService.updateProfile(MEMBER_ID, request);

        assertThat(member.getName()).isEqualTo(request.name());
        assertThat(member.getBirthDate()).isEqualTo(request.birthDate());
        assertThat(member.getGender()).isEqualTo(request.gender());
        assertThat(member.getRoadAddress()).isEqualTo(request.roadAddress());
        assertThat(member.getDetailAddress()).isEqualTo(request.detailAddress());
        assertThat(member.getLatitude()).isEqualByComparingTo(request.latitude());
        assertThat(member.getLongitude()).isEqualByComparingTo(request.longitude());
        assertThat(member.getRegionId()).isEqualTo(request.regionId());
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    @DisplayName("전화번호와 인증 토큰을 함께 전달하면 전화번호를 변경한다")
    void updateProfileWithPhoneNumberSuccess() {
        Member member = createMember(DONG_ID);
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.DONG);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(
                10L,
                "010-1234-5678",
                "phone-verification-token"
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(10L)).willReturn(Optional.of(newDong));
        given(phoneVerificationService.validateAndConsumePhoneChangeToken(
                member,
                request.phoneNumber(),
                request.phoneVerificationToken()
        )).willReturn("01012345678");
        given(memberRepository.existsByPhoneNumberAndIdNot("01012345678", MEMBER_ID))
                .willReturn(false);

        memberService.updateProfile(MEMBER_ID, request);

        assertThat(member.getPhoneNumber()).isEqualTo("01012345678");
        verify(phoneVerificationService).validateAndConsumePhoneChangeToken(
                member,
                request.phoneNumber(),
                request.phoneVerificationToken()
        );
    }

    @Test
    @DisplayName("전화번호만 전달하면 개인정보 수정에 실패한다")
    void throwsExceptionWhenOnlyPhoneNumberIsProvided() {
        Member member = createMember(DONG_ID);
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.DONG);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(10L, "010-1234-5678", null);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(10L)).willReturn(Optional.of(newDong));

        AuthException exception = assertThrows(
                AuthException.class,
                () -> memberService.updateProfile(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(AuthErrorCode.PHONE_CHANGE_REQUEST_INVALID);
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    @DisplayName("인증 토큰만 전달하면 개인정보 수정에 실패한다")
    void throwsExceptionWhenOnlyPhoneVerificationTokenIsProvided() {
        Member member = createMember(DONG_ID);
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.DONG);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(
                10L,
                null,
                "phone-verification-token"
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(10L)).willReturn(Optional.of(newDong));

        AuthException exception = assertThrows(
                AuthException.class,
                () -> memberService.updateProfile(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(AuthErrorCode.PHONE_CHANGE_REQUEST_INVALID);
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    @DisplayName("이미 사용 중인 전화번호로는 개인정보를 수정할 수 없다")
    void throwsExceptionWhenPhoneNumberAlreadyExists() {
        Member member = createMember(DONG_ID);
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.DONG);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(
                10L,
                "010-1234-5678",
                "phone-verification-token"
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(10L)).willReturn(Optional.of(newDong));
        given(phoneVerificationService.validateAndConsumePhoneChangeToken(
                member,
                request.phoneNumber(),
                request.phoneVerificationToken()
        )).willReturn("01012345678");
        given(memberRepository.existsByPhoneNumberAndIdNot("01012345678", MEMBER_ID))
                .willReturn(true);

        MemberException exception = assertThrows(
                MemberException.class,
                () -> memberService.updateProfile(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.DUPLICATE_PHONE_NUMBER);
        verify(phoneVerificationService).validateAndConsumePhoneChangeToken(
                member,
                request.phoneNumber(),
                request.phoneVerificationToken()
        );
        assertThat(member.getPhoneNumber()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 지역으로 개인정보를 수정하면 예외가 발생한다")
    void throwsExceptionWhenUpdateRegionIsNotFound() {
        Member member = createMember(DONG_ID);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(10L);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(10L)).willReturn(Optional.empty());

        RegionException exception = assertThrows(
                RegionException.class,
                () -> memberService.updateProfile(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(RegionErrorCode.REGION_NOT_FOUND);
    }

    @Test
    @DisplayName("동 단위가 아닌 지역으로 개인정보를 수정하면 예외가 발생한다")
    void throwsExceptionWhenUpdateRegionIsNotDong() {
        Member member = createMember(DONG_ID);
        Region sigungu = createRegion(SIGUNGU_ID, SIDO_ID, "미추홀구", RegionLevel.SIGUNGU);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(SIGUNGU_ID);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(SIGUNGU_ID)).willReturn(Optional.of(sigungu));

        RegionException exception = assertThrows(
                RegionException.class,
                () -> memberService.updateProfile(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(RegionErrorCode.REGION_LEVEL_INVALID);
    }

    private Member createMember(Long regionId) {
        return Member.builder()
                .id(MEMBER_ID)
                .name("홍길동")
                .birthDate(LocalDate.of(1950, 1, 1))
                .gender(Gender.MALE)
                .roadAddress("인천광역시 미추홀구")
                .detailAddress("101호")
                .latitude(new BigDecimal("37.4510000"))
                .longitude(new BigDecimal("126.6500000"))
                .regionId(regionId)
                .build();
    }

    private Region createRegion(
            Long id,
            Long parentId,
            String name,
            RegionLevel regionLevel
    ) {
        return Region.builder()
                .id(id)
                .parentId(parentId)
                .name(name)
                .regionLevel(regionLevel)
                .build();
    }

    private MemberReqDTO.UpdateProfile createUpdateRequest(Long regionId) {
        return createUpdateRequest(regionId, null, null);
    }

    private MemberReqDTO.UpdateProfile createUpdateRequest(
            Long regionId,
            String phoneNumber,
            String phoneVerificationToken
    ) {
        return new MemberReqDTO.UpdateProfile(
                "김철수",
                LocalDate.of(1960, 5, 10),
                Gender.MALE,
                "인천광역시 미추홀구 새 주소",
                "202호",
                new BigDecimal("37.4520000"),
                new BigDecimal("126.6510000"),
                regionId,
                phoneNumber,
                phoneVerificationToken
        );
    }
}
