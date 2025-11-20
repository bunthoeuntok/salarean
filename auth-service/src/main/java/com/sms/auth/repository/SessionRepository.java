package com.sms.auth.repository;

import com.sms.auth.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    Optional<Session> findByTokenJti(String tokenJti);

    void deleteByTokenJti(String tokenJti);

    int deleteByExpiresAtBefore(LocalDateTime dateTime);
}
