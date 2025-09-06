package com.example.bankcards.config;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@bank.com");
            admin.setRole(Role.ROLE_ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user created: admin/admin123");
        }

        // Создаем тестового user пользователя
        User user;
        if (userRepository.findByUsername("user").isEmpty()) {
            user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@bank.com");
            user.setRole(Role.ROLE_USER);
            userRepository.save(user);
            System.out.println("User created: user/user123");
        } else {
            user = userRepository.findByUsername("user").get();
        }
        if (cardRepository.findByUserId(user.getId()).isEmpty()) {
            createTestCard(user, "1111222233334444", "User Test 1");
            createTestCard(user, "5555666677778888", "User Test 2");
            System.out.println("Two test cards created for user 'user'");
        }
    }

    private void createTestCard(User user, String cardNumber, String cardHolder) {
        String encryptedNumber = encryptionService.encrypt(cardNumber);

        if (cardRepository.existsByEncryptedCardNumber(encryptedNumber)) {
            return; // если карта уже есть, пропускаем
        }

        Card card = new Card();
        card.setCardNumber(maskCardNumber(cardNumber));
        card.setEncryptedCardNumber(encryptedNumber);
        card.setCardHolder(cardHolder);
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(1000)); // можно выставить стартовый баланс
        card.setUser(user);

        cardRepository.save(card);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber.length() >= 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber;
    }
}