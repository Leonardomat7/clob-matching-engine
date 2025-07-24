# Central Limit Order Book (CLOB)

Este é um projeto de implementação de um Livro de Ordens Central Limitado (CLOB) com Spring Boot em Java 17, desenvolvido como parte de um teste técnico.

## 🔧 Tecnologias Utilizadas

- Java 17
- Spring Boot 3.1.2
- Spring Web
- H2 Database (para testes)
- Swagger/OpenAPI
- JUnit 5

## 📦 Como executar o projeto

```bash
git clone https://github.com/Leonardomat7/clob-matching-engine.git
cd clob-springboot
mvn clean install
mvn spring-boot:run
```

O serviço será iniciado em `http://localhost:8080`.

## 📘 Documentação da API

Uma interface Swagger estará disponível em:

```
http://localhost:8080/swagger-ui.html
```

## 🔐 Endpoints principais

### ✅ Criar Conta

```bash
curl -X POST http://localhost:8080/api/orders/account/{accountId}
```

Exemplo:

```bash
curl -X POST http://localhost:8080/api/orders/account/buyer
```

---

### 💰 Creditar Saldo (apenas via código ou testes)

No momento, a API REST não expõe endpoint de crédito direto. Utilize o método `credit()` do `OrderBookService` para adicionar saldo via testes ou instância manual.

---

### 📥 Registrar Ordem

```bash
curl -X POST http://localhost:8080/api/orders/place \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "buyer",
    "instrument": { "baseAsset": "BTC", "quoteAsset": "BRL" },
    "type": "BUY",
    "price": 50000,
    "quantity": 1
}'
```

---

### ❌ Cancelar Ordem

```bash
curl -X POST http://localhost:8080/api/orders/cancel/{orderId}
```

---

### 📊 Consultar Saldo da Conta

```bash
curl http://localhost:8080/api/orders/balance/{accountId}
```

---

## ✅ Casos de Teste

Os testes estão localizados em `src/test/java/com/exchange/service/OrderBookServiceTest.java`. Eles cobrem:

- Registro e cancelamento de ordens
- Casos de saldo insuficiente
- Matching de ordens
- Créditos e débitos
- Validações negativas

Execute os testes com:

```bash
mvn test
```

---

## 📄 Estrutura Principal

- `OrderBookService`: lógica de negócios do livro de ordens e matching
- `OrderController`: interface REST
- `Account`, `Order`, `Instrument`: modelos de domínio
- `OrderRequestDTO`, `BalanceResponseDTO`: DTOs para transporte

---


---

## 📫 Contato

Para dúvidas ou sugestões:

- Leonardo Matheus Felix da Silva
- Email: leonardomat7@outlook.com
