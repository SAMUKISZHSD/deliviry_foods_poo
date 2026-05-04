# Sistema de Delivery — Guia do UML
---
<img width="1254" height="881" alt="delivery_uml drawio" src="https://github.com/user-attachments/assets/d35842e9-dabf-431e-9404-93c97c5f6f2c" />

## Visão geral

O sistema é dividido em **4 camadas**:

```
┌─────────────────────────────────────────────┐
│              ENTIDADES (model)              │  Os dados do sistema
├─────────────────────────────────────────────┤
│             ENUMERAÇÕES (enum)              │  Estados possíveis
├─────────────────────────────────────────────┤
│              SERVIÇO (service)              │  Regras de negócio
├─────────────────────────────────────────────┤
│         REPOSITÓRIOS (dao + db)             │  Acesso ao banco
└─────────────────────────────────────────────┘
```

---

## 1. Entidades (camada azul)

São as classes que representam os dados reais do sistema. Cada uma vira uma tabela no banco de dados.

### `Restaurante`
Armazena os dados do restaurante parceiro: nome, endereço, telefone e categoria (ex: pizzaria, japonesa).

### `Produto`
Representa um item do cardápio. Cada produto pertence a um restaurante (`restauranteId`), tem nome, descrição e preço.

### `Cliente`
Dados de quem faz o pedido: nome, e-mail, telefone e endereço de entrega.

### `Entregador`
Dados do entregador: nome, telefone, veículo e — muito importante — o **status atual** (disponível, em entrega ou inativo).

### `Pedido`
É a classe central do sistema. Concentra:
- Quem pediu (`clienteId`)
- De qual restaurante (`restauranteId`)
- Quem vai entregar (`entregadorId`)
- O status atual do pedido
- Os valores calculados: subtotal, desconto, taxa de entrega e valor final

### `ItemPedido`
Resolve o relacionamento entre `Pedido` e `Produto` (um pedido tem vários produtos, um produto aparece em vários pedidos). Guarda a quantidade e o preço unitário no momento da compra.

---

## 2. Enumerações (camada amarela)

Definem os valores fixos que um campo pode assumir, evitando erros de digitação e facilitando validações.

### `StatusPedido`
```
AGUARDANDO → CONFIRMADO → EM_PREPARO → SAIU_ENTREGA → ENTREGUE
                                                     ↘ CANCELADO
```

### `StatusEntregador`
```
DISPONIVEL  →  EM_ENTREGA
INATIVO  (fora do sistema)
```

> **Regra de negócio:** ao atribuir um entregador a um pedido, o sistema verifica se ele está `DISPONIVEL`. Se sim, muda o status para `EM_ENTREGA`.

---

## 3. Serviço (camada rosa)

### `CalculadorPedido`
Concentra toda a lógica de cálculo do valor do pedido. Não acessa o banco — só processa números.

**Fluxo de cálculo:**

```
1. Soma os itens          → subtotal
2. Aplica desconto        → baseado no subtotal:
                             > R$ 100 →  5% de desconto
                             > R$ 200 → 10% de desconto
                             > R$ 300 → 15% de desconto
3. Adiciona taxa fixa     → R$ 8,00
4. Retorna valor final    → subtotal - desconto + taxa
```

---

## 4. Repositórios (camada roxa)

### `DatabaseConnection` — Singleton
Gerencia a conexão com o banco de dados. Usa o padrão **Singleton**: só existe uma instância durante toda a execução do programa. Todos os DAOs passam por ela para acessar o banco.

> Para usar o banco fictício (listas em memória), basta alterar apenas esta classe — o resto do sistema não muda.

### DAOs (Data Access Objects)
Cada entidade tem seu próprio DAO responsável pelas operações de banco:

| DAO | Entidade gerenciada | Método extra |
|-----|---------------------|--------------|
| `RestauranteDAO` | `Restaurante` | `relatorioVendas()` |
| `ProdutoDAO` | `Produto` | `listarPorRestaurante()` |
| `ClienteDAO` | `Cliente` | — |
| `EntregadorDAO` | `Entregador` | `listarDisponiveis()` |
| `PedidoDAO` | `Pedido` | `atualizarStatus()` |
| `ItemPedidoDAO` | `ItemPedido` | `excluirPorPedido()` |

Todos os DAOs recebem uma instância de `DatabaseConnection` para executar as queries.

---

## 5. Relacionamentos

| De | Para | Tipo | Leitura |
|----|------|------|---------|
| `Restaurante` | `Produto` | 1 para muitos | Um restaurante tem vários produtos |
| `Cliente` | `Pedido` | 1 para muitos | Um cliente faz vários pedidos |
| `Entregador` | `Pedido` | 1 para muitos | Um entregador realiza várias entregas |
| `Pedido` | `ItemPedido` | 1 para muitos | Um pedido contém vários itens |
| `Produto` | `ItemPedido` | 1 para muitos | Um produto aparece em vários itens |
| `Pedido` | `StatusPedido` | uso | O pedido tem um status |
| `Entregador` | `StatusEntregador` | uso | O entregador tem um status |
| `Pedido` | `CalculadorPedido` | dependência | O pedido usa o serviço de cálculo |
| Todos os DAOs | `DatabaseConnection` | dependência | Todos os DAOs usam a conexão |

---

## 6. Estrutura de pacotes - WIP

```
br.delivery/
├── model/
│   ├── Restaurante.java
│   ├── Produto.java
│   ├── Cliente.java
│   ├── Entregador.java
│   ├── Pedido.java
│   ├── ItemPedido.java
│   ├── StatusPedido.java        ← enum
│   └── StatusEntregador.java    ← enum
├── dao/
│   ├── DatabaseConnection.java  ← singleton
│   ├── RestauranteDAO.java
│   ├── ProdutoDAO.java
│   ├── ClienteDAO.java
│   ├── EntregadorDAO.java
│   ├── PedidoDAO.java
│   └── ItemPedidoDAO.java
├── service/
│   └── CalculadorPedido.java
└── Main.java                    ← menu CLI
```

---

## 7. Como migrar do banco fictício para o PostgreSQL

O sistema foi projetado para facilitar essa troca:

1. **Banco fictício:** `DatabaseConnection` retorna listas em memória, e os DAOs trabalham com essas listas.
2. **PostgreSQL:** basta atualizar `DatabaseConnection` para abrir uma conexão JDBC real (`DriverManager.getConnection(...)`). Os DAOs recebem a conexão e trocam as listas por queries SQL — **sem alterar as classes `model` ou `service`**.

---
