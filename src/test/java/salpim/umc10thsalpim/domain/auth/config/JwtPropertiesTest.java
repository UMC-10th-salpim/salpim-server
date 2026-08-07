package salpim.umc10thsalpim.domain.auth.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    @Test
    void rejectsDocumentedPlaceholderEvenWhenItIsLongEnough() {
        JwtProperties properties = new JwtProperties();
        properties.setSecretKey("REPLACE_WITH_RANDOM_BASE64_SECRET");

        assertThat(properties.isSecretKeySecure()).isFalse();
    }

    @Test
    void acceptsEnvironmentSpecificSecretWithAtLeastThirtyTwoBytes() {
        JwtProperties properties = new JwtProperties();
        properties.setSecretKey("vV5w8Q5yB5gZ2jA7nH9mL4rT6xC3pK8sD1fU0eY7");

        assertThat(properties.isSecretKeySecure()).isTrue();
    }
}
