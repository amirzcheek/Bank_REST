package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findByUserId(Long userId, Pageable pageable);
    List<Card> findByUserId(Long userId);
    Optional<Card> findByEncryptedCardNumber(String encryptedCardNumber);
    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
    Optional<Card> findByIdAndUserId(Long id, Long userId);
}