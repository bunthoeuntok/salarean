package com.sms.auth.repository;

import com.sms.auth.model.LoginAttempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    @Query("SELECT COUNT(la) FROM LoginAttempt la " +
           "WHERE la.identifier = :identifier " +
           "AND la.success = false " +
           "AND la.attemptedAt > :since")
    long countFailedAttemptsSince(@Param("identifier") String identifier,
                                   @Param("since") LocalDateTime since);

    int deleteByAttemptedAtBefore(LocalDateTime dateTime);
}
