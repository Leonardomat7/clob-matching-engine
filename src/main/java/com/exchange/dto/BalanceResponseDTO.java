package com.exchange.dto;

import java.math.BigDecimal;
import java.util.Map;

public record BalanceResponseDTO(String accountId, Map<String, BigDecimal> balances) {}
