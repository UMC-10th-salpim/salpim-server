package salpim.umc10thsalpim.domain.auth.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "login-attempt")
public class LoginAttemptProperties {

    @NotNull
    @Positive
    private Integer phoneMaxFailureCount;

    @NotNull
    @Positive
    private Integer ipMaxFailureCount;

    @NotNull
    @Positive
    private Long lockDurationMillis;
}
