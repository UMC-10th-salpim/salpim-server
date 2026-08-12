package salpim.umc10thsalpim.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import salpim.umc10thsalpim.domain.auth.entity.PhoneVerification;
import salpim.umc10thsalpim.domain.auth.enums.PhoneVerificationPurpose;
import salpim.umc10thsalpim.domain.member.entity.Member;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findByPhoneNumberAndPurpose(
            String phoneNumber,
            PhoneVerificationPurpose purpose
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select phoneVerification
            from PhoneVerification phoneVerification
            where phoneVerification.phoneNumber = :phoneNumber
              and phoneVerification.purpose = :purpose
            """)
    Optional<PhoneVerification> findByPhoneNumberAndPurposeForUpdate(
            @Param("phoneNumber") String phoneNumber,
            @Param("purpose") PhoneVerificationPurpose purpose
    );

    Optional<PhoneVerification> findByMemberAndPhoneNumberAndPurpose(
            Member member,
            String phoneNumber,
            PhoneVerificationPurpose purpose
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select phoneVerification
            from PhoneVerification phoneVerification
            where phoneVerification.member = :member
              and phoneVerification.phoneNumber = :phoneNumber
              and phoneVerification.purpose = :purpose
            """)
    Optional<PhoneVerification> findByMemberAndPhoneNumberAndPurposeForUpdate(
            @Param("member") Member member,
            @Param("phoneNumber") String phoneNumber,
            @Param("purpose") PhoneVerificationPurpose purpose
    );

    void deleteByPhoneNumberAndPurpose(String phoneNumber, PhoneVerificationPurpose purpose);

    void deleteByMember(Member member);
}
