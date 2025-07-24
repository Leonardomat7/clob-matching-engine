package com.exchange.controller;

import com.exchange.dto.BalanceResponseDTO;
import com.exchange.dto.CreditRequestDTO;
import com.exchange.dto.OrderRequestDTO;
import com.exchange.model.Order;
import com.exchange.service.OrderBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderBookService service;

    @PostMapping("/account/{id}")
    public ResponseEntity<String> createAccount(@PathVariable String id) {
        try {
            service.registerAccount(id);
            return ResponseEntity.status(HttpStatus.CREATED).body("Conta criada com sucesso: " + id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDTO dto) {
        try {
            Order order = new Order(
                    dto.accountId(),
                    dto.instrument(),
                    dto.type(),
                    dto.price(),
                    dto.quantity()
            );
            String id = service.placeOrder(order);
            return ResponseEntity.ok("Ordem registrada com ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao registrar ordem: " + e.getMessage());
        }
    }

    @PostMapping("/cancel/{id}")
    public ResponseEntity<String> cancelOrder(@PathVariable UUID id) {
        boolean result = service.cancelOrder(id.toString());
        return result
                ? ResponseEntity.ok("Ordem cancelada com sucesso.")
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ordem não encontrada para cancelamento.");
    }

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<?> getBalance(@PathVariable String accountId) {
        try {
            Map<String, java.math.BigDecimal> balances = service.getBalances(accountId);
            return ResponseEntity.ok(new BalanceResponseDTO(accountId, balances));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Conta não encontrada.");
        }
    }

    @PostMapping("/credit")
    public ResponseEntity<String> credit(@RequestBody CreditRequestDTO dto) {
        try {
            service.credit(dto.accountId(), dto.asset(), dto.amount());
            return ResponseEntity.ok("Saldo creditado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao creditar saldo: " + e.getMessage());
        }
    }

}
