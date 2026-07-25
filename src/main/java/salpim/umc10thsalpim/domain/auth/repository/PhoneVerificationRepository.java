package salpim.umc10thsalpim.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import salpim.umc10thsalpim.domain.auth.entity.PhoneVerification;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;
import salpim.umc10thsalpim.domain.member.entity.Member;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findByPhoneNumberAndPurpose(
            String phoneNumber,
            PhoneVerificationPurpose purpose
    );

    void deleteByPhoneNumberAndPurpose(String phoneNumber, PhoneVerificationPurpose purpose);
}
