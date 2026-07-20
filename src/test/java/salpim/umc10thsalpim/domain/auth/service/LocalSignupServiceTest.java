package salpim.umc10thsalpim.domain.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.Gender;
import salpim.umc10thsalpim.domain.member.exception.MemberErrorCode;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;
import salpim.umc10thsalpim.domain.region.entity.Region;
import salpim.umc10thsalpim.domain.region.enums.RegionLevel;
import salpim.umc10thsalpim.domain.region.exception.RegionErrorCode;
import salpim.umc10thsalpim.domain.region.exception.RegionException;
import salpim.umc10thsalpim.domain.region.repository.RegionRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalSignupServiceTest {

    private static final Long REGION_ID = 1L;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private PhoneVerificationService phoneVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LocalSignupService localSignupService;

    @Test
    void signupSavesMemberWithRegion() {
        AuthReqDTO.LocalSignup request = validRequest("123456", "Seoul");
        Region region = region();

        when(memberRepository.existsByPhoneNumber("01031768867")).thenReturn(false);
        when(regionRepository.findById(REGION_ID)).thenReturn(Optional.of(region));
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        localSignupService.signup(request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getRegion()).isSameAs(region);
    }

    @Test
    void signupFailsWhenRegionDoesNotExist() {
        AuthReqDTO.LocalSignup request = validRequest("123456", "Seoul");

        when(memberRepository.existsByPhoneNumber("01031768867")).thenReturn(false);
        when(regionRepository.findById(REGION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> localSignupService.signup(request))
                .isInstanceOfSatisfying(RegionException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_NOT_FOUND));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void signupFailsWhenRegionIsNotLeaf() {
        AuthReqDTO.LocalSignup request = validRequest("123456", "Seoul");
        Region city = Region.create(null, "Goyang", RegionLevel.CITY);

        when(memberRepository.existsByPhoneNumber("01031768867")).thenReturn(false);
        when(regionRepository.findById(REGION_ID)).thenReturn(Optional.of(city));

        assertThatThrownBy(() -> localSignupService.signup(request))
                .isInstanceOfSatisfying(RegionException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(RegionErrorCode.REGION_NOT_LEAF));
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void signupFailsWhenLocalPasswordIsBlank() {
        AuthReqDTO.LocalSignup request = validRequest(" ", "Seoul");

        assertThatThrownBy(() -> localSignupService.signup(request))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.REQUIRED_LOCAL_PASSWORD));
    }

    @Test
    void signupFailsWhenPasswordRecoveryAnswerIsBlank() {
        AuthReqDTO.LocalSignup request = validRequest("123456", " ");

        assertThatThrownBy(() -> localSignupService.signup(request))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.REQUIRED_PASSWORD_RECOVERY_ANSWER));
    }

    private AuthReqDTO.LocalSignup validRequest(String password, String passwordAnswer) {
        return new AuthReqDTO.LocalSignup(
                "Jihong",
                LocalDate.of(2002, 3, 11),
                Gender.MALE,
                "010-3176-8867",
                password,
                "Goyang Deogyang Hwarang-ro 28",
                "B",
                37.1234567,
                126.1234567,
                REGION_ID,
                passwordAnswer
        );
    }

    private Region region() {
        return Region.create(null, "Hwajeon", RegionLevel.EUP_MYEON_DONG);
    }
}
