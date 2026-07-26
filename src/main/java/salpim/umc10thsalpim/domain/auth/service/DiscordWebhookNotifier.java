package salpim.umc10thsalpim.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import salpim.umc10thsalpim.domain.auth.config.DiscordProperties;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordWebhookNotifier {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient;
    private final DiscordProperties discordProperties;

    public void sendVerificationCode(
            String maskedPhoneNumber,
            String code,
            PhoneVerificationPurpose purpose
    ) {
        if (!StringUtils.hasText(discordProperties.getOtpWebhookUrl())) {
            return;
        }

        String verificationType = purpose == PhoneVerificationPurpose.SIGNUP
                ? "회원가입"
                : "전화번호 변경";
        String message = "[테스트 전용 " + verificationType + " 인증번호]\n"
                + "전화번호: " + maskedPhoneNumber + "\n"
                + "인증번호: " + code + "\n"
                + "유효시간: 5분";

        try {
            webClient.post()
                    .uri(discordProperties.getOtpWebhookUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new DiscordWebhookMessage(message))
                    .retrieve()
                    .toBodilessEntity()
                    .block(REQUEST_TIMEOUT);
        } catch (Exception exception) {
            log.warn(
                    "Discord OTP webhook delivery failed. exceptionType={}",
                    exception.getClass().getSimpleName()
            );
        }
    }

    private record DiscordWebhookMessage(String content) {
    }
}
