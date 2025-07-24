package com.exchange.dto;

import java.math.BigDecimal;

public record CreditRequestDTO(String accountId, String asset, BigDecimal amount) {}
