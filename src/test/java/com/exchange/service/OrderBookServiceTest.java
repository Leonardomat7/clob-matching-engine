package com.exchange.service;

import com.exchange.model.Instrument;
import com.exchange.model.Order;
import com.exchange.model.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class OrderBookServiceTest {

    private OrderBookService service;

    @BeforeEach
    void setup() {
        service = new OrderBookService();
    }

    @Test
    void testRegisterAccount_Valid() {
        service.registerAccount("user1");
        assertNotNull(service.getBalances("user1"));
    }

    @Test
    void testRegisterAccount_Invalid() {
        assertThrows(IllegalArgumentException.class, () -> service.registerAccount(""));
        assertThrows(IllegalArgumentException.class, () -> service.registerAccount(null));
    }

    @Test
    void testPlaceBuyOrder_SufficientBalance() {
        service.registerAccount("buyer");
        service.credit("buyer", "BRL", new BigDecimal("1000000"));

        Order buy = new Order("buyer", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("50000"), new BigDecimal("0.5"));

        String orderId = service.placeOrder(buy);
        assertNotNull(orderId);

        Map<String, BigDecimal> balances = service.getBalances("buyer");
        assertEquals(0, balances.get("BRL").compareTo(new BigDecimal("975000.00")));
    }


    @Test
    void testPlaceSellOrder_SufficientBalance() {
        service.registerAccount("seller");
        service.credit("seller", "BTC", new BigDecimal("2"));

        Order sell = new Order("seller", new Instrument("BTC", "BRL"), OrderType.SELL,
                new BigDecimal("60000"), new BigDecimal("1"));

        String orderId = service.placeOrder(sell);
        assertNotNull(orderId);

        Map<String, BigDecimal> balances = service.getBalances("seller");
        assertEquals(0, balances.get("BTC").compareTo(new BigDecimal("1.00")));
    }


    @Test
    void testCancelOrder_Buy() {
        service.registerAccount("buyer");
        service.credit("buyer", "BRL", new BigDecimal("100000"));

        Order buy = new Order("buyer", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("50000"), new BigDecimal("1"));

        String orderId = service.placeOrder(buy);
        boolean result = service.cancelOrder(orderId);

        assertTrue(result);
        assertEquals(0, service.getBalances("buyer").get("BRL").compareTo(new BigDecimal("100000.00")));
    }


    @Test
    void testCancelOrder_Sell() {
        service.registerAccount("seller");
        service.credit("seller", "BTC", new BigDecimal("1"));

        Order sell = new Order("seller", new Instrument("BTC", "BRL"), OrderType.SELL,
                new BigDecimal("60000"), new BigDecimal("1"));

        String orderId = service.placeOrder(sell);
        boolean result = service.cancelOrder(orderId);

        assertTrue(result);
        assertEquals(0, service.getBalances("seller").get("BTC").compareTo(new BigDecimal("1.00")));
    }


    @Test
    void testOrderMatching() {
        service.registerAccount("buyer");
        service.registerAccount("seller");

        service.credit("buyer", "BRL", new BigDecimal("50000"));
        service.credit("seller", "BTC", new BigDecimal("1"));

        Order sell = new Order("seller", new Instrument("BTC", "BRL"), OrderType.SELL,
                new BigDecimal("50000"), new BigDecimal("1"));
        service.placeOrder(sell);

        Order buy = new Order("buyer", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("50000"), new BigDecimal("1"));
        service.placeOrder(buy);

        assertEquals(0, service.getBalances("buyer").get("BTC").compareTo(new BigDecimal("1.00")));
        assertEquals(0, service.getBalances("seller").get("BRL").compareTo(new BigDecimal("50000.00")));
    }

    @Test
    void testValidationFails_InvalidData() {
        assertThrows(NullPointerException.class, () -> {
            new Order(null, null, null, null, null);
        });
    }

    @Test
    void testValidationFails_MissingBalance() {
        service.registerAccount("buyer");

        Order order = new Order("buyer", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("50000"), new BigDecimal("1"));

        assertThrows(IllegalStateException.class, () -> service.placeOrder(order));
    }

    @Test
    void testGetOpenOrders() {
        service.registerAccount("buyer");
        service.credit("buyer", "BRL", new BigDecimal("100000"));

        Order buy = new Order("buyer", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("40000"), new BigDecimal("1"));
        service.placeOrder(buy);

        List<Order> openBuy = service.getOpenBuyOrders();
        List<Order> openSell = service.getOpenSellOrders();

        assertEquals(1, openBuy.size());
        assertTrue(openSell.isEmpty());
    }

    @Test
    void testPlaceOrder_PriceZero_ThrowsException() {
        service.registerAccount("user");
        service.credit("user", "BRL", new BigDecimal("100000"));

        Order order = new Order("user", new Instrument("BTC", "BRL"), OrderType.BUY,
                BigDecimal.ZERO, new BigDecimal("1"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.placeOrder(order);
        });
        assertEquals("Preço deve ser positivo.", exception.getMessage());
    }

    @Test
    void testPlaceOrder_PriceNegative_ThrowsException() {
        service.registerAccount("user");
        service.credit("user", "BRL", new BigDecimal("100000"));

        Order order = new Order("user", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("-1"), new BigDecimal("1"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.placeOrder(order);
        });
        assertEquals("Preço deve ser positivo.", exception.getMessage());
    }

    @Test
    void testPlaceOrder_QuantityZero_ThrowsException() {
        service.registerAccount("user");
        service.credit("user", "BRL", new BigDecimal("100000"));

        Order order = new Order("user", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("50000"), BigDecimal.ZERO);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.placeOrder(order);
        });
        assertEquals("Quantidade deve ser positiva.", exception.getMessage());
    }

    @Test
    void testPlaceOrder_QuantityNegative_ThrowsException() {
        service.registerAccount("user");
        service.credit("user", "BRL", new BigDecimal("100000"));

        Order order = new Order("user", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("50000"), new BigDecimal("-1"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.placeOrder(order);
        });
        assertEquals("Quantidade deve ser positiva.", exception.getMessage());
    }

    @Test
    void testPlaceOrder_UnregisteredAccount_ThrowsException() {
        // NÃO registra a conta

        Order order = new Order("ghost", new Instrument("BTC", "BRL"), OrderType.BUY,
                new BigDecimal("50000"), new BigDecimal("1"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.placeOrder(order);
        });
        assertEquals("Conta não registrada.", exception.getMessage());
    }

    @Test
    void testDebit_Successful() {
        service.registerAccount("trader");
        service.credit("trader", "BRL", new BigDecimal("1000"));

        service.debit("trader", "BRL", new BigDecimal("500"));

        Map<String, BigDecimal> balances = service.getBalances("trader");
        assertEquals(0, balances.get("BRL").compareTo(new BigDecimal("500.00")));
    }


    @Test
    void testDebit_AccountNotFound_ThrowsException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.debit("ghost", "BRL", new BigDecimal("100"));
        });

        assertEquals("Conta não encontrada: ghost", exception.getMessage());
    }

    @Test
    void testDebit_InsufficientBalance_ThrowsException() {
        service.registerAccount("trader");
        service.credit("trader", "BTC", new BigDecimal("0.1")); // Saldo insuficiente

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            service.debit("trader", "BTC", new BigDecimal("1"));
        });

        assertEquals("Saldo insuficiente para débito.", exception.getMessage());
    }
}
