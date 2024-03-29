package com.dzalex.skillshuffle.repositories;

import com.dzalex.skillshuffle.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByToken(String token);
    void deleteByToken(String token);
}
