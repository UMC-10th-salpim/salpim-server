package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import salpim.umc10thsalpim.domain.auth.dto.AuthReqDTO;
import salpim.umc10thsalpim.domain.auth.dto.AuthResDTO;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.member.entity.Member;
import salpim.umc10thsalpim.domain.member.enums.SocialProvider;
import salpim.umc10thsalpim.domain.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalLoginService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public AuthResDTO.TokenResult login(AuthReqDTO.LocalLogin request, String clientIp) {
        String normalizedPhoneNumber = normalizePhoneNumber(request.phoneNumber());
        loginAttemptService.validateAllowed(normalizedPhoneNumber, clientIp);

        Member member = memberRepository.findByPhoneNumber(normalizedPhoneNumber).orElse(null);

        if (!hasValidLocalPassword(member, request.password())) {
            loginAttemptService.recordFailure(normalizedPhoneNumber, clientIp);
            throw new AuthException(AuthErrorCode.INVALID_LOGIN_CREDENTIALS);
        }

        loginAttemptService.clearFailures(normalizedPhoneNumber, clientIp);
        return tokenService.issueLoginTokens(member);
    }

    private boolean hasValidLocalPassword(Member member, String rawPassword) {
        return member != null
                && member.getLoginType() == SocialProvider.LOCAL
                && member.getPassword() != null
                && passwordEncoder.matches(rawPassword, member.getPassword());
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "").trim();
    }
}
