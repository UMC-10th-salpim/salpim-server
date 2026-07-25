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

    @Transactional
    public AuthResDTO.TokenResult login(AuthReqDTO.LocalLogin request) {
        String normalizedPhoneNumber = normalizePhoneNumber(request.phoneNumber());
        Member member = memberRepository.findByPhoneNumber(normalizedPhoneNumber)
                .orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_MEMBER_NOT_FOUND));

        validateLocalPassword(member, request.password());
        return tokenService.issueLoginTokens(member);
    }

    private void validateLocalPassword(Member member, String rawPassword) {
        if (member.getLoginType() != SocialProvider.LOCAL
                || member.getPassword() == null
                || !passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.replace("-", "").trim();
    }
}
