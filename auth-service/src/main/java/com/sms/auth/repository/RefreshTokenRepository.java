package com.sms.auth.repository;

import com.sms.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByIdAndUserId(UUID id, UUID userId);

    List<RefreshToken> findByUserIdAndExpiresAtAfter(UUID userId, LocalDateTime now);

    void deleteByUserIdAndIdNot(UUID userId, UUID tokenId);

    void deleteByUserId(UUID userId);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
