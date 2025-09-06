package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Page<CardDto> getUserCards(Long userId, Pageable pageable) {
        return cardRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String encryptedNumber = encryptionService.encrypt(request.getCardNumber());

        if (cardRepository.existsByEncryptedCardNumber(encryptedNumber)) {
            throw new IllegalArgumentException("Card number already exists");
        }

        String maskedNumber = maskCardNumber(request.getCardNumber());

        Card card = new Card();
        card.setCardNumber(maskedNumber);
        card.setEncryptedCardNumber(encryptedNumber);
        card.setCardHolder(request.getCardHolder());
        card.setExpirationDate(request.getExpirationDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        return convertToDto(savedCard);
    }

    @Transactional
    public CardDto blockCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.ROLE_USER && !card.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot block another user's card");
        }

        // Меняем статус
        card.setStatus(CardStatus.BLOCKED);
        return convertToDto(cardRepository.save(card));
    }

    @Transactional
    public CardDto activateCard(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (!card.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only activate your own cards");
        }

        card.setStatus(CardStatus.ACTIVE);
        return convertToDto(cardRepository.save(card));
    }

    @Transactional
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long cardId, Long userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (!card.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only view balance of your own cards");
        }

        return card.getBalance();
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() >= 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber;
    }

    public void transferBetweenCards(Long userId, TransferRequest request) {
        Card fromCard = cardRepository.findByIdAndUserId(request.getFromCardId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Source card not found or not yours"));

        Card toCard = cardRepository.findByIdAndUserId(request.getToCardId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Target card not found or not yours"));

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        // списание и зачисление
        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    @Transactional
    public CardDto depositToCard(Long cardId, BigDecimal amount) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found"));

        if (card.getStatus() == CardStatus.BLOCKED || card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalArgumentException("Cannot deposit to a blocked or expired card");
        }

        card.setBalance(card.getBalance().add(amount));
        return convertToDto(cardRepository.save(card));
    }

    @Transactional(readOnly = true)
    public Page<CardDto> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Transactional
    public String transferBetweenCards(User user, TransferRequest request) {
        Card fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new IllegalArgumentException("From card not found"));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new IllegalArgumentException("To card not found"));

        if (!fromCard.getUser().getId().equals(user.getId()) ||
                !toCard.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only transfer between your own cards");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // ✅ сохраняем транзакцию
        Transaction tx = new Transaction();
        tx.setType(TransactionType.TRANSFER);
        tx.setAmount(request.getAmount());
        tx.setFromCard(fromCard);
        tx.setToCard(toCard);
        transactionRepository.save(tx);

        return "Transfer successful";
    }

    private CardDto convertToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setCardNumber(card.getCardNumber());
        dto.setCardHolder(card.getCardHolder());
        dto.setExpirationDate(card.getExpirationDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        dto.setUserId(card.getUser().getId());
        return dto;
    }
}