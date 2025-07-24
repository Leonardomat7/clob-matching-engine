package com.exchange.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Account {

    private final String id;
    private final Map<String, BigDecimal> balances;

    public Account(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Account ID must not be null or blank.");
        }
        this.id = id;
        this.balances = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public synchronized void credit(String asset, BigDecimal amount) {
        validateAssetAndAmount(asset, amount);
        balances.merge(asset, amount, BigDecimal::add);
    }

    public synchronized boolean debit(String asset, BigDecimal amount) {
        validateAssetAndAmount(asset, amount);
        BigDecimal current = balances.getOrDefault(asset, BigDecimal.ZERO);
        if (current.compareTo(amount) >= 0) {
            balances.put(asset, current.subtract(amount));
            return true;
        }
        return false;
    }

    public synchronized BigDecimal getBalance(String asset) {
        return balances.getOrDefault(asset, BigDecimal.ZERO);
    }

    public synchronized Map<String, BigDecimal> getAllBalances() {
        return Collections.unmodifiableMap(new HashMap<>(balances));
    }

    private void validateAssetAndAmount(String asset, BigDecimal amount) {
        if (asset == null || asset.isBlank()) {
            throw new IllegalArgumentException("Asset must not be null or blank.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", balances=" + balances +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account account = (Account) o;
        return id.equals(account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
