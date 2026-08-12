package salpim.umc10thsalpim.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.exception.MemberException;
import salpim.umc10thsalpim.domain.member.exception.code.MemberErrorCode;

@Component
@RequiredArgsConstructor
public class PasswordPolicy {

    private final PasswordEncoder passwordEncoder;

    public void validateNewPasswordIsDifferent(Member member, String newPassword) {
        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new MemberException(MemberErrorCode.PASSWORD_SAME_AS_CURRENT);
        }
    }
}
