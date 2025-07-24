package com.exchange.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class Order {

    private final String id;
    private final String accountId;
    private final Instrument instrument;
    private final OrderType type;
    private final BigDecimal price;
    private BigDecimal quantity;

    public Order(String accountId, Instrument instrument, OrderType type, BigDecimal price, BigDecimal quantity) {
        this.id = UUID.randomUUID().toString();
        this.accountId = Objects.requireNonNull(accountId);
        this.instrument = Objects.requireNonNull(instrument);
        this.type = Objects.requireNonNull(type);
        this.price = Objects.requireNonNull(price);
        this.quantity = Objects.requireNonNull(quantity);
    }

    public String getId() {
        return id;
    }

    public String getAccountId() {
        return accountId;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public OrderType getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void decreaseQuantity(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Decrease must be positive.");
        if (amount.compareTo(quantity) > 0) throw new IllegalArgumentException("Insufficient quantity.");
        this.quantity = this.quantity.subtract(amount);
    }
}
