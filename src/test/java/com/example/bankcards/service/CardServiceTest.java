package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardService cardService;

    @Test
    void testCreateCard_Success() {
        User user = new User();
        user.setId(1L);

        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(1L);
        request.setCardNumber("1234567812345678");
        request.setCardHolder("Test User");
        request.setExpirationDate(LocalDate.now().plusYears(2));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(encryptionService.encrypt("1234567812345678")).thenReturn("encrypted");
        when(cardRepository.existsByEncryptedCardNumber("encrypted")).thenReturn(false);

        Card savedCard = new Card();
        savedCard.setId(1L);
        savedCard.setCardNumber("**** **** **** 5678");
        savedCard.setEncryptedCardNumber("encrypted");
        savedCard.setUser(user);
        savedCard.setBalance(BigDecimal.ZERO);

        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardDto result = cardService.createCard(request);

        assertEquals("**** **** **** 5678", result.getCardNumber());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void testCreateCard_UserNotFound() {
        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(99L);
        request.setCardNumber("1234567812345678");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(request));
    }

    @Test
    void testCreateCard_CardAlreadyExists() {
        User user = new User();
        user.setId(1L);

        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(1L);
        request.setCardNumber("1234567812345678");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(encryptionService.encrypt("1234567812345678")).thenReturn("encrypted");
        when(cardRepository.existsByEncryptedCardNumber("encrypted")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(request));
    }

    @Test
    void testDepositToCard_Success() {
        User user = new User();
        user.setId(1L);

        Card card = new Card();
        card.setId(1L);
        card.setBalance(BigDecimal.valueOf(100));
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardDto result = cardService.depositToCard(1L, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(150), result.getBalance());
    }

    @Test
    void testDepositToCard_BlockedOrExpired() {
        User user = new User();
        user.setId(1L);

        Card cardBlocked = new Card();
        cardBlocked.setId(1L);
        cardBlocked.setBalance(BigDecimal.valueOf(100));
        cardBlocked.setStatus(CardStatus.BLOCKED);
        cardBlocked.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardBlocked));
        assertThrows(IllegalArgumentException.class, () -> cardService.depositToCard(1L, BigDecimal.valueOf(50)));

        Card cardExpired = new Card();
        cardExpired.setId(2L);
        cardExpired.setBalance(BigDecimal.valueOf(100));
        cardExpired.setStatus(CardStatus.EXPIRED);
        cardExpired.setUser(user);

        when(cardRepository.findById(2L)).thenReturn(Optional.of(cardExpired));
        assertThrows(IllegalArgumentException.class, () -> cardService.depositToCard(2L, BigDecimal.valueOf(50)));
    }

    @Test
    void testBlockCard_Success() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.ROLE_USER);

        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        CardDto result = cardService.blockCard(1L, 1L);
        assertEquals(CardStatus.BLOCKED, result.getStatus());
    }

    @Test
    void testBlockCard_OtherUser() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.ROLE_USER);

        User otherUser = new User();
        otherUser.setId(2L);

        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(otherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> cardService.blockCard(1L, 1L));
    }

    @Test
    void testActivateCard_OtherUser() {
        User user = new User();
        user.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        Card card = new Card();
        card.setId(1L);
        card.setUser(otherUser);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.activateCard(1L, 1L));
    }

    @Test
    void testGetCardBalance_OtherUser() {
        User user = new User();
        user.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        Card card = new Card();
        card.setId(1L);
        card.setUser(otherUser);
        card.setBalance(BigDecimal.valueOf(100));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThrows(IllegalArgumentException.class, () -> cardService.getCardBalance(1L, 1L));
    }

    @Test
    void testTransferBetweenCards_Success() {
        User user = new User();
        user.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(user);
        fromCard.setBalance(BigDecimal.valueOf(200));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(user);
        toCard.setBalance(BigDecimal.valueOf(100));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(inv -> inv.getArgument(0));

        cardService.transferBetweenCards(user, new TransferRequest(1L, 2L, BigDecimal.valueOf(50)));

        assertEquals(BigDecimal.valueOf(150), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(150), toCard.getBalance());
    }

    @Test
    void testTransferBetweenCards_InsufficientFunds() {
        User user = new User();
        user.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(user);
        fromCard.setBalance(BigDecimal.valueOf(20));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(user);
        toCard.setBalance(BigDecimal.valueOf(100));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(IllegalArgumentException.class,
                () -> cardService.transferBetweenCards(user, new TransferRequest(1L, 2L, BigDecimal.valueOf(50))));
    }

    @Test
    void testTransferBetweenCards_AccessDenied() {
        User user = new User();
        user.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(otherUser);
        fromCard.setBalance(BigDecimal.valueOf(200));

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(user);
        toCard.setBalance(BigDecimal.valueOf(100));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        assertThrows(AccessDeniedException.class,
                () -> cardService.transferBetweenCards(user, new TransferRequest(1L, 2L, BigDecimal.valueOf(50))));
    }
}
