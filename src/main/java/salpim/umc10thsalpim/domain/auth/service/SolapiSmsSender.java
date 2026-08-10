package salpim.umc10thsalpim.domain.auth.service;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import salpim.umc10thsalpim.domain.auth.exception.AuthException;
import salpim.umc10thsalpim.domain.auth.exception.code.AuthErrorCode;
import salpim.umc10thsalpim.global.config.SolapiConfig;

@Slf4j
@Component
public class SolapiSmsSender {

    private final SolapiConfig solapiConfig;
    private final DefaultMessageService messageService;

    public SolapiSmsSender(SolapiConfig solapiConfig) {
        this.solapiConfig = solapiConfig;
        this.messageService = SolapiClient.INSTANCE.createInstance(
                solapiConfig.getApiKey(),
                solapiConfig.getApiSecret()
        );
    }

    public void sendVerificationCode(String phoneNumber, String code) {
        Message message = new Message();
        message.setFrom(solapiConfig.getSenderNumber());
        message.setTo(phoneNumber);
        message.setText("[살핌] 인증번호는 " + code + "입니다. 5분 내에 입력해주세요.");

        try {
            messageService.send(message);
        } catch (Exception exception) {
            log.warn(
                    "SMS verification code delivery failed. PhoneNumber={}, exceptionType={}",
                    maskPhoneNumber(phoneNumber),
                    exception.getClass().getSimpleName()
            );
            throw new AuthException(AuthErrorCode.SMS_SEND_FAILED);
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }

        return "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
