package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.entity.PhoneVerification;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.repository.PhoneVerificationRepository;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final String PHONE_NUMBER = "010-1234-5678";
    private static final String NORMALIZED_PHONE_NUMBER = "01012345678";
    private static final String VERIFICATION_CODE = "123456";
    private static final String VERIFICATION_TOKEN = "phone-verification-token";
    private static final String TOKEN_HASH = "encoded-token";

    @Mock
    private PhoneVerificationRepository phoneVerificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PhoneVerificationService phoneVerificationService;

    @Test
    @DisplayName("전화번호 변경용 인증번호를 발송한다")
    void sendPhoneChangeVerificationCodeSuccess() {
        Member member = createMember();
        LocalDateTime beforeRequest = LocalDateTime.now();

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(memberRepository.existsByPhoneNumberAndIdNot(NORMALIZED_PHONE_NUMBER, MEMBER_ID))
                .willReturn(false);
        given(phoneVerificationRepository.findByMemberAndPhoneNumberAndPurpose(
                member,
                NORMALIZED_PHONE_NUMBER,
                PhoneVerificationPurpose.PHONE_CHANGE
        )).willReturn(Optional.empty());

        phoneVerificationService.sendPhoneChangeVerificationCode(MEMBER_ID, PHONE_NUMBER);

        verify(phoneVerificationRepository).deleteByPhoneNumberAndPurpose(
                NORMALIZED_PHONE_NUMBER,
                PhoneVerificationPurpose.PHONE_CHANGE
        );
        verify(phoneVerificationRepository).flush();

        ArgumentCaptor<PhoneVerification> verificationCaptor = ArgumentCaptor.forClass(
                PhoneVerification.class
        );
        verify(phoneVerificationRepository).save(verificationCaptor.capture());

        PhoneVerification savedVerification = verificationCaptor.getValue();
        assertThat(savedVerification.getMember()).isSameAs(member);
        assertThat(savedVerification.getPhoneNumber()).isEqualTo(NORMALIZED_PHONE_NUMBER);
        assertThat(savedVerification.getPurpose()).isEqualTo(PhoneVerificationPurpose.PHONE_CHANGE);
        assertThat(savedVerification.getCode()).hasSize(6).containsOnlyDigits();
        assertThat(savedVerification.getVerified()).isFalse();
        assertThat(savedVerification.getExpiredAt()).isAfter(beforeRequest.plusMinutes(4));
    }

    @Test
    @DisplayName("올바른 인증번호를 검증하면 전화번호 변경 인증 토큰을 발급한다")
    void verifyPhoneChangeCodeSuccess() {
        Member member = createMember();
        PhoneVerification verification = createVerification(
                member,
                LocalDateTime.now().plusMinutes(5),
                false,
                null,
                null,
                null
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(phoneVerificationRepository.findByMemberAndPhoneNumberAndPurpose(
                member,
                NORMALIZED_PHONE_NUMBER,
                PhoneVerificationPurpose.PHONE_CHANGE
        )).willReturn(Optional.of(verification));
        given(passwordEncoder.encode(anyString())).willReturn(TOKEN_HASH);

        AuthResDTO.PhoneChangeVerifyResult result = phoneVerificationService
                .verifyPhoneChangeCode(MEMBER_ID, PHONE_NUMBER, VERIFICATION_CODE);

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder).encode(tokenCaptor.capture());

        assertThat(result.phoneVerificationToken()).isEqualTo(tokenCaptor.getValue());
        assertThat(verification.getVerified()).isTrue();
        assertThat(verification.getVerificationTokenHash()).isEqualTo(TOKEN_HASH);
        assertThat(verification.getTokenExpiredAt()).isAfter(LocalDateTime.now().plusMinutes(9));
        assertThat(verification.getUsedAt()).isNull();
    }

    @Test
    @DisplayName("만료된 인증번호로는 전화번호 변경 인증 토큰을 발급할 수 없다")
    void throwsExceptionWhenPhoneChangeCodeIsExpired() {
        Member member = createMember();
        PhoneVerification verification = createVerification(
                member,
                LocalDateTime.now().minusMinutes(1),
                false,
                null,
                null,
                null
        );

        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(phoneVerificationRepository.findByMemberAndPhoneNumberAndPurpose(
                member,
                NORMALIZED_PHONE_NUMBER,
                PhoneVerificationPurpose.PHONE_CHANGE
        )).willReturn(Optional.of(verification));

        AuthException exception = assertThrows(
                AuthException.class,
                () -> phoneVerificationService.verifyPhoneChangeCode(
                        MEMBER_ID,
                        PHONE_NUMBER,
                        VERIFICATION_CODE
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(AuthErrorCode.EXPIRED_VERIFICATION_CODE);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("유효한 전화번호 변경 인증 토큰은 한 번만 사용할 수 있다")
    void validateAndConsumePhoneChangeTokenSuccess() {
        Member member = createMember();
        PhoneVerification verification = createVerification(
                member,
                LocalDateTime.now().plusMinutes(5),
                true,
                TOKEN_HASH,
                LocalDateTime.now().plusMinutes(10),
                null
        );

        given(phoneVerificationRepository.findByMemberAndPhoneNumberAndPurpose(
                member,
                NORMALIZED_PHONE_NUMBER,
                PhoneVerificationPurpose.PHONE_CHANGE
        )).willReturn(Optional.of(verification));
        given(passwordEncoder.matches(VERIFICATION_TOKEN, TOKEN_HASH)).willReturn(true);

        String result = phoneVerificationService.validateAndConsumePhoneChangeToken(
                member,
                PHONE_NUMBER,
                VERIFICATION_TOKEN
        );

        assertThat(result).isEqualTo(NORMALIZED_PHONE_NUMBER);
        assertThat(verification.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("만료된 전화번호 변경 인증 토큰은 사용할 수 없다")
    void throwsExceptionWhenPhoneChangeTokenIsExpired() {
        Member member = createMember();
        PhoneVerification verification = createVerification(
                member,
                LocalDateTime.now().plusMinutes(5),
                true,
                TOKEN_HASH,
                LocalDateTime.now().minusMinutes(1),
                null
        );

        given(phoneVerificationRepository.findByMemberAndPhoneNumberAndPurpose(
                member,
                NORMALIZED_PHONE_NUMBER,
                PhoneVerificationPurpose.PHONE_CHANGE
        )).willReturn(Optional.of(verification));

        AuthException exception = assertThrows(
                AuthException.class,
                () -> phoneVerificationService.validateAndConsumePhoneChangeToken(
                        member,
                        PHONE_NUMBER,
                        VERIFICATION_TOKEN
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(AuthErrorCode.EXPIRED_PHONE_VERIFICATION_TOKEN);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("이미 사용한 전화번호 변경 인증 토큰은 사용할 수 없다")
    void throwsExceptionWhenPhoneChangeTokenIsAlreadyUsed() {
        Member member = createMember();
        PhoneVerification verification = createVerification(
                member,
                LocalDateTime.now().plusMinutes(5),
                true,
                TOKEN_HASH,
                LocalDateTime.now().plusMinutes(10),
                LocalDateTime.now().minusMinutes(1)
        );

        given(phoneVerificationRepository.findByMemberAndPhoneNumberAndPurpose(
                member,
                NORMALIZED_PHONE_NUMBER,
                PhoneVerificationPurpose.PHONE_CHANGE
        )).willReturn(Optional.of(verification));

        AuthException exception = assertThrows(
                AuthException.class,
                () -> phoneVerificationService.validateAndConsumePhoneChangeToken(
                        member,
                        PHONE_NUMBER,
                        VERIFICATION_TOKEN
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_PHONE_VERIFICATION_TOKEN);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("일치하지 않는 전화번호 변경 인증 토큰은 사용할 수 없다")
    void throwsExceptionWhenPhoneChangeTokenDoesNotMatch() {
        Member member = createMember();
        PhoneVerification verification = createVerification(
                member,
                LocalDateTime.now().plusMinutes(5),
                true,
                TOKEN_HASH,
                LocalDateTime.now().plusMinutes(10),
                null
        );

        given(phoneVerificationRepository.findByMemberAndPhoneNumberAndPurpose(
                member,
                NORMALIZED_PHONE_NUMBER,
                PhoneVerificationPurpose.PHONE_CHANGE
        )).willReturn(Optional.of(verification));
        given(passwordEncoder.matches(VERIFICATION_TOKEN, TOKEN_HASH)).willReturn(false);

        AuthException exception = assertThrows(
                AuthException.class,
                () -> phoneVerificationService.validateAndConsumePhoneChangeToken(
                        member,
                        PHONE_NUMBER,
                        VERIFICATION_TOKEN
                )
        );

        assertThat(exception.getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_PHONE_VERIFICATION_TOKEN);
        assertThat(verification.getUsedAt()).isNull();
    }

    private Member createMember() {
        return Member.builder()
                .id(MEMBER_ID)
                .build();
    }

    private PhoneVerification createVerification(
            Member member,
            LocalDateTime expiredAt,
            boolean verified,
            String verificationTokenHash,
            LocalDateTime tokenExpiredAt,
            LocalDateTime usedAt
    ) {
        return PhoneVerification.builder()
                .member(member)
                .phoneNumber(NORMALIZED_PHONE_NUMBER)
                .purpose(PhoneVerificationPurpose.PHONE_CHANGE)
                .code(VERIFICATION_CODE)
                .expiredAt(expiredAt)
                .verified(verified)
                .verificationTokenHash(verificationTokenHash)
                .tokenExpiredAt(tokenExpiredAt)
                .usedAt(usedAt)
                .build();
    }
}
