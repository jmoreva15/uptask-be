package com.uptask.otp.repository;

import com.uptask.otp.entity.OtpCode;
import com.uptask.otp.entity.OtpType;
import com.uptask.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findTopByUserAndTypeAndUsedFalseOrderByCreatedAtDesc(User user, OtpType type);

    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.user = :user AND o.type = :type AND o.used = false")
    void invalidateAllByUserAndType(User user, OtpType type);
}
