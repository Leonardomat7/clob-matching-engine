package com.exchange.service;

import com.exchange.model.Account;
import com.exchange.model.Instrument;
import com.exchange.model.Order;
import com.exchange.model.OrderType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;


@Service
public class OrderBookService {

    private final Map<String, Order> activeOrders = new HashMap<>();
    private final PriorityQueue<Order> buyOrders;
    private final PriorityQueue<Order> sellOrders;
    private final Map<String, Account> accounts = new HashMap<>();
    private final Instrument instrument = new Instrument("BTC", "BRL");

    public OrderBookService() {
        buyOrders = new PriorityQueue<>(Comparator.comparing(Order::getPrice).reversed());
        sellOrders = new PriorityQueue<>(Comparator.comparing(Order::getPrice));
    }

    public void registerAccount(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID da conta é obrigatório.");
        }
        accounts.putIfAbsent(id, new Account(id));
    }

    public String placeOrder(Order order) {
        validateOrder(order);

        Account account = accounts.get(order.getAccountId());

        if (order.getType() == OrderType.BUY) {
            BigDecimal totalCost = order.getPrice().multiply(order.getQuantity());
            if (!account.debit(instrument.quoteAsset(), totalCost)) {
                throw new IllegalStateException("Saldo insuficiente para comprar.");
            }
            buyOrders.offer(order);
        } else {
            if (!account.debit(instrument.baseAsset(), order.getQuantity())) {
                throw new IllegalStateException("Saldo insuficiente para vender.");
            }
            sellOrders.offer(order);
        }

        activeOrders.put(order.getId(), order);
        matchOrders();
        return order.getId();
    }

    public boolean cancelOrder(String orderId) {
        Order order = activeOrders.remove(orderId);
        if (order == null) return false;

        Account account = accounts.get(order.getAccountId());
        if (order.getType() == OrderType.BUY) {
            buyOrders.remove(order);
            BigDecimal refund = order.getPrice().multiply(order.getQuantity());
            account.credit(instrument.quoteAsset(), refund);
        } else {
            sellOrders.remove(order);
            account.credit(instrument.baseAsset(), order.getQuantity());
        }

        return true;
    }

    private void matchOrders() {
        while (!buyOrders.isEmpty() && !sellOrders.isEmpty()) {
            Order buy = buyOrders.peek();
            Order sell = sellOrders.peek();

            if (buy.getPrice().compareTo(sell.getPrice()) >= 0) {
                BigDecimal tradedQty = buy.getQuantity().min(sell.getQuantity());
                BigDecimal tradePrice = sell.getPrice();

                Account buyer = accounts.get(buy.getAccountId());
                Account seller = accounts.get(sell.getAccountId());

                buyer.credit(instrument.baseAsset(), tradedQty);
                seller.credit(instrument.quoteAsset(), tradePrice.multiply(tradedQty));

                buy.decreaseQuantity(tradedQty);
                sell.decreaseQuantity(tradedQty);

                if (buy.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                    buyOrders.poll();
                    activeOrders.remove(buy.getId());
                }

                if (sell.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
                    sellOrders.poll();
                    activeOrders.remove(sell.getId());
                }

            } else {
                break;
            }
        }
    }

    public Map<String, BigDecimal> getBalances(String accountId) {
        Account account = accounts.get(accountId);
        if (account == null) throw new IllegalArgumentException("Conta inexistente.");
        return account.getAllBalances();
    }

    public List<Order> getOpenBuyOrders() {
        return new ArrayList<>(buyOrders);
    }

    public List<Order> getOpenSellOrders() {
        return new ArrayList<>(sellOrders);
    }

    private void validateOrder(Order order) {
        Objects.requireNonNull(order, "Ordem não pode ser nula.");
        Objects.requireNonNull(order.getAccountId(), "Conta da ordem não pode ser nula.");
        Objects.requireNonNull(order.getPrice(), "Preço não pode ser nulo.");
        Objects.requireNonNull(order.getQuantity(), "Quantidade não pode ser nula.");

        if (order.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Preço deve ser positivo.");

        if (order.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Quantidade deve ser positiva.");

        if (!accounts.containsKey(order.getAccountId()))
            throw new IllegalArgumentException("Conta não registrada.");
    }


    public void credit(String accountId, String asset, BigDecimal amount) {
        Account account = accounts.get(accountId);
        if (account == null) throw new IllegalArgumentException("Conta não encontrada: " + accountId);
        account.credit(asset, amount);
    }

    public void debit(String accountId, String asset, BigDecimal amount) {
        Account account = accounts.get(accountId);
        if (account == null) throw new IllegalArgumentException("Conta não encontrada: " + accountId);
        if (!account.debit(asset, amount)) {
            throw new IllegalStateException("Saldo insuficiente para débito.");
        }
    }
}
