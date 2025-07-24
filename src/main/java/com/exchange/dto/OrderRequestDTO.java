package com.exchange.dto;

import com.exchange.model.OrderType;
import com.exchange.model.Instrument;

import java.math.BigDecimal;

public record OrderRequestDTO(
        String accountId,
        Instrument instrument,
        OrderType type,
        BigDecimal price,
        BigDecimal quantity
) {}
