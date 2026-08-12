package salpim.umc10thsalpim.domain.member.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordPolicy passwordPolicy;

    @Test
    void rejectsNewPasswordThatMatchesCurrentPassword() {
        Member member = Member.builder().password("encoded-password").build();
        when(passwordEncoder.matches("same-password", "encoded-password")).thenReturn(true);

        assertThatThrownBy(() ->
                passwordPolicy.validateNewPasswordIsDifferent(member, "same-password"))
                .isInstanceOfSatisfying(MemberException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(MemberErrorCode.PASSWORD_SAME_AS_CURRENT));
    }

    @Test
    void allowsNewPasswordThatDiffersFromCurrentPassword() {
        Member member = Member.builder().password("encoded-password").build();
        when(passwordEncoder.matches("new-password", "encoded-password")).thenReturn(false);

        assertThatCode(() -> passwordPolicy.validateNewPasswordIsDifferent(
                member,
                "new-password"
        )).doesNotThrowAnyException();
    }
}
