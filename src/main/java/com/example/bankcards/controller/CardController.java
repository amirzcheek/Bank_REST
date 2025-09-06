package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.dto.DepositRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<Page<CardDto>> getUserCards(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getUserCards(user.getId(), pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardRequest request) {
        return ResponseEntity.ok(cardService.createCard(request));
    }

    @PostMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CardDto> blockCard(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.blockCard(id, user.getId()));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CardDto> activateCard(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.activateCard(id, user.getId()));
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cardService.getCardBalance(id, user.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> transferBetweenCards(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request) {

        cardService.transferBetweenCards(user.getId(), request);
        return ResponseEntity.ok("Transfer successful");
    }

    @PostMapping("/{id}/deposit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> depositToCard(
            @PathVariable Long id,
            @Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(cardService.depositToCard(id, request.getAmount()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardDto>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(cardService.getAllCards(pageable));
    }
}