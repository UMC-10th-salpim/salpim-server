package salpim.umc10thsalpim.domain.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.service.PhoneVerificationService;
import salpim.umc10thsalpim.domain.member.dto.MemberReqDTO;
import salpim.umc10thsalpim.domain.member.dto.MemberResDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.enums.PasswordVerificationMethod;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.exception.code.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;
import salpim.umc10thsalpim.domain.region.service.RegionQueryService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

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
    private RegionQueryService regionQueryService;

    @Mock
    private PhoneVerificationService phoneVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원의 동 지역을 기준으로 마이페이지 정보를 조회한다")
    void getMyPageSuccess() {
        Member member = createMember(DONG_ID);

        Region dong = createRegion(DONG_ID, SIGUNGU_ID, "용현동", RegionLevel.ADMINISTRATIVE_AREA);
        Region sigungu = createRegion(SIGUNGU_ID, SIDO_ID, "미추홀구", RegionLevel.SIGUNGU);
        Region sido = createRegion(SIDO_ID, null, "인천광역시", RegionLevel.SIDO);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(DONG_ID)).willReturn(Optional.of(dong));
        given(regionQueryService.findAncestorRegionOrThrow(dong, RegionLevel.SIDO)).willReturn(sido);
        given(regionQueryService.findAncestorRegionOrThrow(dong, RegionLevel.SIGUNGU)).willReturn(sigungu);

        MemberResDTO.MyPageInfo result = memberService.getMyPage(MEMBER_ID);

        assertThat(result).isEqualTo(
                new MemberResDTO.MyPageInfo("홍길동", "인천광역시", "미추홀구")
        );
    }

    @Test
    @DisplayName("일반구를 거치는 회원도 시도와 시군구를 조회한다")
    void getMyPageSuccessWithGeneralGu() {
        Long generalGuId = 3L;
        Long administrativeAreaId = 4L;
        Member member = createMember(administrativeAreaId);

        Region sido = createRegion(SIDO_ID, null, "Gyeonggi-do", RegionLevel.SIDO);
        Region sigungu = createRegion(SIGUNGU_ID, SIDO_ID, "Goyang-si", RegionLevel.SIGUNGU);
        Region generalGu = createRegion(generalGuId, SIGUNGU_ID, "Deogyang-gu", RegionLevel.GENERAL_GU);
        Region administrativeArea = createRegion(
                administrativeAreaId,
                generalGuId,
                "Hwajeong-dong",
                RegionLevel.ADMINISTRATIVE_AREA
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(administrativeAreaId)).willReturn(Optional.of(administrativeArea));
        given(regionQueryService.findAncestorRegionOrThrow(administrativeArea, RegionLevel.SIDO))
                .willReturn(sido);
        given(regionQueryService.findAncestorRegionOrThrow(administrativeArea, RegionLevel.SIGUNGU))
                .willReturn(sigungu);

        MemberResDTO.MyPageInfo result = memberService.getMyPage(MEMBER_ID);

        assertThat(result.sido()).isEqualTo("Gyeonggi-do");
        assertThat(result.sigungu()).isEqualTo("Goyang-si");
        assertThat(generalGu.getRegionLevel()).isEqualTo(RegionLevel.GENERAL_GU);
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
        Region dong = createRegion(DONG_ID, null, "용현동", RegionLevel.ADMINISTRATIVE_AREA);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(DONG_ID)).willReturn(Optional.of(dong));
        given(regionQueryService.findAncestorRegionOrThrow(dong, RegionLevel.SIDO))
                .willThrow(new RegionException(RegionErrorCode.REGION_HIERARCHY_INVALID));

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
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.ADMINISTRATIVE_AREA);
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
    @DisplayName("개인정보 수정 시 텍스트 입력값을 정규화한다")
    void normalizesTextFieldsWhenUpdatingProfile() {
        Member member = createMember(DONG_ID);
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.ADMINISTRATIVE_AREA);
        MemberReqDTO.UpdateProfile request = new MemberReqDTO.UpdateProfile(
                " 김철수 ",
                LocalDate.of(1960, 5, 10),
                Gender.MALE,
                " 인천광역시 미추홀구 새 주소 ",
                "   ",
                new BigDecimal("37.4520000"),
                new BigDecimal("126.6510000"),
                10L,
                null,
                null
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(10L)).willReturn(Optional.of(newDong));

        memberService.updateProfile(MEMBER_ID, request);

        assertThat(member.getName()).isEqualTo("김철수");
        assertThat(member.getRoadAddress()).isEqualTo("인천광역시 미추홀구 새 주소");
        assertThat(member.getDetailAddress()).isNull();
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    @DisplayName("전화번호와 인증 토큰을 함께 전달하면 전화번호를 변경한다")
    void updateProfileWithPhoneNumberSuccess() {
        Member member = createMember(DONG_ID);
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.ADMINISTRATIVE_AREA);
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
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.ADMINISTRATIVE_AREA);
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
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.ADMINISTRATIVE_AREA);
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
        Region newDong = createRegion(10L, SIGUNGU_ID, "주안동", RegionLevel.ADMINISTRATIVE_AREA);
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
    void throwsExceptionWhenUpdateRegionIsNotAdministrativeArea() {
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

    @Test
    @DisplayName("시도는 회원 지역으로 수정할 수 없다")
    void throwsExceptionWhenUpdateRegionIsSido() {
        Member member = createMember(DONG_ID);
        Region sido = createRegion(SIDO_ID, null, "Incheon", RegionLevel.SIDO);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(SIDO_ID);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(SIDO_ID)).willReturn(Optional.of(sido));

        RegionException exception = assertThrows(
                RegionException.class,
                () -> memberService.updateProfile(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_LEVEL_INVALID);
    }

    @Test
    @DisplayName("일반구는 회원 지역으로 수정할 수 없다")
    void throwsExceptionWhenUpdateRegionIsGeneralGu() {
        Member member = createMember(DONG_ID);
        Region generalGu = createRegion(10L, SIGUNGU_ID, "Deogyang-gu", RegionLevel.GENERAL_GU);
        MemberReqDTO.UpdateProfile request = createUpdateRequest(10L);

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(regionRepository.findById(10L)).willReturn(Optional.of(generalGu));

        RegionException exception = assertThrows(
                RegionException.class,
                () -> memberService.updateProfile(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_LEVEL_INVALID);
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

    @Test
    @DisplayName("현재 비밀번호가 일치하면 검증에 성공한다")
    void verifyCurrentPasswordSuccess() {
        Member member = createLocalMember("encoded-password", "encoded-answer");
        MemberReqDTO.VerifyCurrentPassword request =
                new MemberReqDTO.VerifyCurrentPassword("123456");

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("123456", "encoded-password")).willReturn(true);

        MemberResDTO.PasswordVerificationResult result =
                memberService.verifyCurrentPassword(MEMBER_ID, request);

        assertThat(result.isVerified()).isTrue();
        verify(passwordEncoder).matches("123456", "encoded-password");
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 예외가 발생한다")
    void throwsExceptionWhenCurrentPasswordDoesNotMatch() {
        Member member = createLocalMember("encoded-password", "encoded-answer");
        MemberReqDTO.VerifyCurrentPassword request =
                new MemberReqDTO.VerifyCurrentPassword("123456");

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("123456", "encoded-password")).willReturn(false);

        MemberException exception = assertThrows(
                MemberException.class,
                () -> memberService.verifyCurrentPassword(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.PASSWORD_MISMATCH);
    }

    @Test
    @DisplayName("복구 답변이 일치하면 검증에 성공한다")
    void verifyRecoveryAnswerSuccess() {
        Member member = createLocalMember("encoded-password", "encoded-answer");
        MemberReqDTO.VerifyRecoveryAnswer request =
                new MemberReqDTO.VerifyRecoveryAnswer("봄");

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("봄", "encoded-answer")).willReturn(true);

        MemberResDTO.PasswordVerificationResult result =
                memberService.verifyRecoveryAnswer(MEMBER_ID, request);

        assertThat(result.isVerified()).isTrue();
        verify(passwordEncoder).matches("봄", "encoded-answer");
    }

    @Test
    @DisplayName("복구 답변 입력값의 앞뒤 공백을 제거하고 검증한다")
    void trimsRecoveryAnswerBeforeVerification() {
        Member member = createLocalMember("encoded-password", "encoded-answer");
        MemberReqDTO.VerifyRecoveryAnswer request =
                new MemberReqDTO.VerifyRecoveryAnswer(" answer ");

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("answer", "encoded-answer")).willReturn(true);

        MemberResDTO.PasswordVerificationResult result =
                memberService.verifyRecoveryAnswer(MEMBER_ID, request);

        assertThat(result.isVerified()).isTrue();
        verify(passwordEncoder).matches("answer", "encoded-answer");
    }

    @Test
    @DisplayName("복구 답변이 일치하지 않으면 예외가 발생한다")
    void throwsExceptionWhenRecoveryAnswerDoesNotMatch() {
        Member member = createLocalMember("encoded-password", "encoded-answer");
        MemberReqDTO.VerifyRecoveryAnswer request =
                new MemberReqDTO.VerifyRecoveryAnswer("여름");

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("여름", "encoded-answer")).willReturn(false);

        MemberException exception = assertThrows(
                MemberException.class,
                () -> memberService.verifyRecoveryAnswer(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.RECOVERY_ANSWER_MISMATCH);
    }

    @Test
    @DisplayName("카카오 로그인 회원이 비밀번호를 검증하면 예외가 발생한다")
    void throwsExceptionWhenKakaoMemberVerifiesPassword() {
        Member member = Member.builder()
                .id(MEMBER_ID)
                .loginType(SocialProvider.KAKAO)
                .build();

        MemberReqDTO.VerifyCurrentPassword request =
                new MemberReqDTO.VerifyCurrentPassword("123456");

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

        MemberException exception = assertThrows(
                MemberException.class,
                () -> memberService.verifyCurrentPassword(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.PASSWORD_CHANGE_NOT_SUPPORTED);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("현재 비밀번호 검증 방식으로 비밀번호를 변경한다")
    void changePasswordWithCurrentPasswordSuccess() {
        Member member = createLocalMember("encoded-password", "encoded-answer");

        MemberReqDTO.ChangePassword request = new MemberReqDTO.ChangePassword(
                PasswordVerificationMethod.CURRENT_PASSWORD,
                "123456",
                null,
                "654321"
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("123456", "encoded-password")).willReturn(true);
        given(passwordEncoder.encode("654321")).willReturn("new-encoded-password");

        memberService.changePassword(MEMBER_ID, request);

        assertThat(member.getPassword()).isEqualTo("new-encoded-password");
        verify(passwordEncoder).matches("123456", "encoded-password");
        verify(passwordEncoder).encode("654321");
    }

    @Test
    @DisplayName("복구 답변 검증 방식으로 비밀번호를 변경한다")
    void changePasswordWithRecoveryAnswerSuccess() {
        Member member = createLocalMember("encoded-password", "encoded-answer");

        MemberReqDTO.ChangePassword request = new MemberReqDTO.ChangePassword(
                PasswordVerificationMethod.RECOVERY_ANSWER,
                null,
                "봄",
                "654321"
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(passwordEncoder.matches("봄", "encoded-answer")).willReturn(true);
        given(passwordEncoder.encode("654321")).willReturn("new-encoded-password");

        memberService.changePassword(MEMBER_ID, request);

        assertThat(member.getPassword()).isEqualTo("new-encoded-password");
        verify(passwordEncoder).matches("봄", "encoded-answer");
        verify(passwordEncoder).encode("654321");
    }

    @Test
    @DisplayName("검증 방식에 맞는 값을 보내지 않으면 비밀번호 변경에 실패한다")
    void throwsExceptionWhenVerificationValueIsMissing() {
        Member member = createLocalMember("encoded-password", "encoded-answer");

        MemberReqDTO.ChangePassword request = new MemberReqDTO.ChangePassword(
                PasswordVerificationMethod.CURRENT_PASSWORD,
                null,
                null,
                "654321"
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

        MemberException exception = assertThrows(
                MemberException.class,
                () -> memberService.changePassword(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.INVALID_PASSWORD_VERIFICATION);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("현재 비밀번호와 복구 답변을 함께 보내면 비밀번호 변경에 실패한다")
    void throwsExceptionWhenBothVerificationValuesAreProvided() {
        Member member = createLocalMember("encoded-password", "encoded-answer");

        MemberReqDTO.ChangePassword request = new MemberReqDTO.ChangePassword(
                PasswordVerificationMethod.CURRENT_PASSWORD,
                "123456",
                "봄",
                "654321"
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

        MemberException exception = assertThrows(
                MemberException.class,
                () -> memberService.changePassword(MEMBER_ID, request)
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(MemberErrorCode.INVALID_PASSWORD_VERIFICATION);
        verifyNoInteractions(passwordEncoder);
    }

    private Member createLocalMember(
            String encodedPassword,
            String encodedRecoveryAnswer
    ) {
        return Member.builder()
                .id(MEMBER_ID)
                .loginType(SocialProvider.LOCAL)
                .password(encodedPassword)
                .passwordRecoveryAnswer(encodedRecoveryAnswer)
                .build();
    }
}
