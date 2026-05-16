# Sistema de Delivery — Documentação de Banco de Dados

> **Projeto:** delivery-system | **Versão:** 1.0-SNAPSHOT  
> **Pacote:** `br.delivery` | **Banco:** PostgreSQL + HikariCP 5.1.0

---

## Sumário

1. [Visão Geral](#1-visão-geral)
2. [Schema do Banco de Dados](#2-schema-do-banco-de-dados)
3. [Relacionamentos e Foreign Keys](#3-relacionamentos-e-foreign-keys)
4. [DatabaseConnection — Singleton + HikariCP](#4-databaseconnection--singleton--hikaricp)
5. [Camada DAO](#5-camada-dao)
6. [Tratamento de Erros](#6-tratamento-de-erros)
7. [Configuração Inicial do Ambiente](#7-configuração-inicial-do-ambiente)

---

## 1. Visão Geral

O sistema utiliza **PostgreSQL** como banco relacional, com pool de conexões gerenciado pelo **HikariCP**. O acesso ao banco é feito exclusivamente pela camada **DAO (Data Access Object)**, que separa a lógica de persistência das regras de negócio.

### Tecnologias

| Tecnologia | Versão | Função |
|---|---|---|
| PostgreSQL | 42.7.3 (driver) | Banco de dados relacional principal |
| HikariCP | 5.1.0 | Pool de conexões JDBC de alta performance |
| JDBC | Java 17+ | API de acesso a banco de dados Java |
| Maven | pom.xml | Gerenciamento de dependências e build |

### Estrutura de Pacotes

```
br.delivery/
├── dao/
│   ├── DatabaseConnection.java   ← Singleton + Pool HikariCP
│   ├── RestauranteDAO.java
│   ├── ProdutoDAO.java
│   ├── ClienteDAO.java
│   ├── EntregadorDAO.java
│   ├── PedidoDAO.java
│   └── ItemPedidoDAO.java
├── model/
│   ├── Restaurante.java  |  Produto.java    |  Cliente.java
│   ├── Entregador.java   |  Pedido.java     |  ItemPedido.java
│   ├── StatusPedido.java        ← enum
│   └── StatusEntregador.java    ← enum
├── service/
│   └── CalculadorPedido.java
└── Main.java
```

---

## 2. Schema do Banco de Dados

As tabelas devem ser criadas na ordem abaixo para respeitar as Foreign Keys. O script completo está em `query.sql` na raiz do projeto.

---

### 2.1 Tabela: `restaurante`

Armazena os dados dos restaurantes parceiros do sistema.

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | `SERIAL` | PK, NOT NULL | Identificador único autoincremental |
| `nome` | `VARCHAR(120)` | NOT NULL | Nome do restaurante |
| `endereco` | `VARCHAR(255)` | NOT NULL | Endereço completo |
| `telefone` | `VARCHAR(20)` | NOT NULL | Telefone de contato |
| `categoria` | `VARCHAR(60)` | NOT NULL | Tipo de culinária (ex: Pizzaria, Japonesa) |

```sql
CREATE TABLE IF NOT EXISTS restaurante (
    id        SERIAL       PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    endereco  VARCHAR(255) NOT NULL,
    telefone  VARCHAR(20)  NOT NULL,
    categoria VARCHAR(60)  NOT NULL
);
```

---

### 2.2 Tabela: `produto`

Representa os itens do cardápio. Cada produto pertence a um restaurante.

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | `SERIAL` | PK, NOT NULL | Identificador único |
| `restaurante_id` | `INTEGER` | FK → restaurante(id) | Restaurante dono do produto |
| `nome` | `VARCHAR(120)` | NOT NULL | Nome do produto |
| `descricao` | `TEXT` | — | Descrição detalhada (opcional) |
| `preco` | `NUMERIC(10,2)` | NOT NULL, >= 0 | Preço unitário em reais |

```sql
CREATE TABLE IF NOT EXISTS produto (
    id             SERIAL         PRIMARY KEY,
    restaurante_id INTEGER        NOT NULL REFERENCES restaurante(id) ON DELETE CASCADE,
    nome           VARCHAR(120)   NOT NULL,
    descricao      TEXT,
    preco          NUMERIC(10, 2) NOT NULL CHECK (preco >= 0)
);

CREATE INDEX IF NOT EXISTS idx_produto_restaurante ON produto(restaurante_id);
```

> **Índice:** `idx_produto_restaurante` — acelera busca de produtos por restaurante.

---

### 2.3 Tabela: `cliente`

Dados de quem realiza os pedidos.

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | `SERIAL` | PK, NOT NULL | Identificador único |
| `nome` | `VARCHAR(120)` | NOT NULL | Nome completo do cliente |
| `email` | `VARCHAR(120)` | NOT NULL, UNIQUE | E-mail único por cliente |
| `telefone` | `VARCHAR(20)` | NOT NULL | Telefone de contato |
| `endereco` | `VARCHAR(255)` | NOT NULL | Endereço de entrega padrão |

```sql
CREATE TABLE IF NOT EXISTS cliente (
    id        SERIAL       PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    email     VARCHAR(120) NOT NULL UNIQUE,
    telefone  VARCHAR(20)  NOT NULL,
    endereco  VARCHAR(255) NOT NULL
);
```

---

### 2.4 Tabela: `entregador`

Dados dos entregadores e seu status no sistema.

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | `SERIAL` | PK, NOT NULL | Identificador único |
| `nome` | `VARCHAR(120)` | NOT NULL | Nome do entregador |
| `telefone` | `VARCHAR(20)` | NOT NULL | Telefone de contato |
| `veiculo` | `VARCHAR(60)` | NOT NULL | Tipo de veículo (moto, bicicleta...) |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT `'DISPONIVEL'`, CHECK | `DISPONIVEL` \| `EM_ENTREGA` \| `INATIVO` |

```sql
CREATE TABLE IF NOT EXISTS entregador (
    id       SERIAL       PRIMARY KEY,
    nome     VARCHAR(120) NOT NULL,
    telefone VARCHAR(20)  NOT NULL,
    veiculo  VARCHAR(60)  NOT NULL,
    status   VARCHAR(20)  NOT NULL DEFAULT 'DISPONIVEL'
                 CHECK (status IN ('DISPONIVEL', 'EM_ENTREGA', 'INATIVO'))
);

CREATE INDEX IF NOT EXISTS idx_entregador_status ON entregador(status);
```

> **CHECK:** o banco rejeita qualquer status fora dos três valores permitidos.  
> **Índice:** `idx_entregador_status` — acelera a busca de entregadores disponíveis.

---

### 2.5 Tabela: `pedido`

Entidade central do sistema. Conecta cliente, restaurante e entregador, e armazena todos os valores financeiros.

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | `SERIAL` | PK, NOT NULL | Identificador único |
| `cliente_id` | `INTEGER` | FK → cliente(id) | Cliente que fez o pedido |
| `restaurante_id` | `INTEGER` | FK → restaurante(id) | Restaurante do pedido |
| `entregador_id` | `INTEGER` | FK → entregador(id), **NULL OK** | Entregador (atribuído após confirmação) |
| `status` | `VARCHAR(20)` | NOT NULL, DEFAULT `'AGUARDANDO'`, CHECK | Status atual do pedido |
| `subtotal` | `NUMERIC(10,2)` | NOT NULL, DEFAULT 0 | Soma dos itens antes do desconto |
| `desconto` | `NUMERIC(10,2)` | NOT NULL, DEFAULT 0 | Valor de desconto aplicado |
| `taxa_entrega` | `NUMERIC(10,2)` | NOT NULL, DEFAULT 8.00 | Taxa fixa de entrega (R$ 8,00) |
| `valor_final` | `NUMERIC(10,2)` | NOT NULL, DEFAULT 0 | `subtotal - desconto + taxa_entrega` |
| `data_criacao` | `TIMESTAMP` | NOT NULL, DEFAULT NOW() | Data e hora de criação automática |

```sql
CREATE TABLE IF NOT EXISTS pedido (
    id             SERIAL         PRIMARY KEY,
    cliente_id     INTEGER        NOT NULL REFERENCES cliente(id),
    restaurante_id INTEGER        NOT NULL REFERENCES restaurante(id),
    entregador_id  INTEGER                 REFERENCES entregador(id),
    status         VARCHAR(20)    NOT NULL DEFAULT 'AGUARDANDO'
                       CHECK (status IN ('AGUARDANDO','CONFIRMADO','EM_PREPARO',
                                         'SAIU_ENTREGA','ENTREGUE','CANCELADO')),
    subtotal       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    desconto       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    taxa_entrega   NUMERIC(10, 2) NOT NULL DEFAULT 8.00,
    valor_final    NUMERIC(10, 2) NOT NULL DEFAULT 0,
    data_criacao   TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pedido_cliente     ON pedido(cliente_id);
CREATE INDEX IF NOT EXISTS idx_pedido_restaurante ON pedido(restaurante_id);
```

**Fluxo de status:**

```
AGUARDANDO → CONFIRMADO → EM_PREPARO → SAIU_ENTREGA → ENTREGUE
                                                    ↘ CANCELADO
```

---

### 2.6 Tabela: `item_pedido`

Tabela associativa entre `pedido` e `produto`. Resolve o relacionamento N:N e registra o preço no momento da compra.

| Coluna | Tipo | Restrições | Descrição |
|---|---|---|---|
| `id` | `SERIAL` | PK, NOT NULL | Identificador único |
| `pedido_id` | `INTEGER` | FK → pedido(id), **CASCADE** | Pedido ao qual o item pertence |
| `produto_id` | `INTEGER` | FK → produto(id) | Produto adicionado ao pedido |
| `quantidade` | `INTEGER` | NOT NULL, > 0 | Quantidade do produto |
| `preco_unitario` | `NUMERIC(10,2)` | NOT NULL, >= 0 | Preço do produto no momento da compra |

```sql
CREATE TABLE IF NOT EXISTS item_pedido (
    id              SERIAL         PRIMARY KEY,
    pedido_id       INTEGER        NOT NULL REFERENCES pedido(id) ON DELETE CASCADE,
    produto_id      INTEGER        NOT NULL REFERENCES produto(id),
    quantidade      INTEGER        NOT NULL CHECK (quantidade > 0),
    preco_unitario  NUMERIC(10, 2) NOT NULL CHECK (preco_unitario >= 0)
);

CREATE INDEX IF NOT EXISTS idx_item_pedido_pedido ON item_pedido(pedido_id);
```

> **ON DELETE CASCADE:** se o pedido for deletado, todos os seus itens são removidos automaticamente pelo banco.

---

## 3. Relacionamentos e Foreign Keys

| Coluna (FK) | Referencia | On Delete | Observação |
|---|---|---|---|
| `produto.restaurante_id` | `restaurante(id)` | CASCADE | Deleta produtos ao remover o restaurante |
| `pedido.cliente_id` | `cliente(id)` | RESTRICT | Não permite deletar cliente com pedidos |
| `pedido.restaurante_id` | `restaurante(id)` | RESTRICT | Não permite deletar restaurante com pedidos |
| `pedido.entregador_id` | `entregador(id)` | SET NULL | Anula a referência se o entregador for removido |
| `item_pedido.pedido_id` | `pedido(id)` | CASCADE | Deleta itens ao remover o pedido |
| `item_pedido.produto_id` | `produto(id)` | RESTRICT | Não permite deletar produto com itens vinculados |

**Diagrama de relacionamentos:**

```
restaurante ──< produto
restaurante ──< pedido
cliente     ──< pedido
entregador  ──< pedido
pedido      ──< item_pedido
produto     ──< item_pedido
```

---

## 4. DatabaseConnection — Singleton + HikariCP

Classe responsável por gerenciar o pool de conexões JDBC. Implementa o padrão **Singleton**: apenas uma instância existe durante toda a execução da aplicação.

### 4.1 Configuração da Conexão

| Propriedade | Padrão | Variável de Ambiente |
|---|---|---|
| URL | `jdbc:postgresql://localhost:5432/delivery_food` | `DB_URL` |
| Usuário | `postgres` | `DB_USER` |
| Senha | `admin` | `DB_PASSWORD` |

> Em produção, sempre configure as variáveis de ambiente para não expor credenciais no código.

### 4.2 Parâmetros do Pool HikariCP

| Parâmetro | Valor | Descrição |
|---|---|---|
| `maximumPoolSize` | 10 | Máximo de conexões simultâneas abertas |
| `minimumIdle` | 2 | Conexões mínimas mantidas ativas |
| `connectionTimeout` | 30.000 ms | Tempo máximo aguardando conexão do pool |
| `idleTimeout` | 600.000 ms | Tempo até fechar conexão ociosa (10 min) |
| `maxLifetime` | 1.800.000 ms | Tempo de vida máximo da conexão (30 min) |
| `connectionTestQuery` | `SELECT 1` | Query de validação de conexão ativa |

### 4.3 Métodos Públicos

| Método | Retorno | Descrição |
|---|---|---|
| `getInstance()` | `DatabaseConnection` | Retorna a instância Singleton (thread-safe com `synchronized`) |
| `getConnection()` | `Connection` | Retorna uma conexão do pool — usar com `try-with-resources` |
| `testarConexao()` | `boolean` | Verifica se o banco está acessível |
| `close()` | `void` | Encerra o pool ao finalizar a aplicação |

### 4.4 Padrão de Uso nos DAOs

Todas as operações de banco usam `try-with-resources` para garantir que a conexão retorne ao pool automaticamente:

```java
try (Connection conn = db.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {

    ps.setString(1, valor);
    ResultSet rs = ps.executeQuery();
    // processar resultado

} // conexão retorna ao pool automaticamente
```

---

## 5. Camada DAO

Cada entidade tem seu próprio DAO responsável por todas as operações de banco. Todos recebem uma instância de `DatabaseConnection` via construtor (injeção de dependência).

---

### 5.1 ClienteDAO

Gerencia operações CRUD da tabela `cliente`.

| Método | Retorno | Descrição |
|---|---|---|
| `inserir(Cliente c)` | `void` | INSERT com `RETURNING id` — define o ID no objeto após inserção |
| `listarTodos()` | `List<Cliente>` | SELECT todos os clientes ordenados por id |
| `buscarPorId(int id)` | `Optional<Cliente>` | SELECT por PK — retorna `Optional.empty()` se não encontrado |
| `atualizar(Cliente c)` | `boolean` | UPDATE de todos os campos — retorna `true` se afetou ao menos 1 linha |
| `excluir(int id)` | `boolean` | DELETE por id |

---

### 5.2 EntregadorDAO

Gerencia operações CRUD da tabela `entregador`, com suporte a filtro por status.

| Método | Retorno | Descrição |
|---|---|---|
| `inserir(Entregador e)` | `void` | INSERT com `RETURNING id` — converte enum `StatusEntregador` para String |
| `listarTodos()` | `List<Entregador>` | SELECT todos os entregadores ordenados por id |
| `listarDisponiveis()` | `List<Entregador>` | SELECT WHERE `status = 'DISPONIVEL'` — usado para atribuir entregador a pedido |
| `buscarPorId(int id)` | `Optional<Entregador>` | SELECT por PK |
| `atualizar(Entregador e)` | `boolean` | UPDATE de todos os campos, incluindo status |
| `excluir(int id)` | `boolean` | DELETE por id |

> **Mapeamento enum:** o campo `status` é salvo como `VARCHAR` no banco. A conversão é feita via `StatusEntregador.valueOf()` (banco → Java) e `.name()` (Java → banco).

---

### 5.3 ProdutoDAO

Gerencia operações CRUD da tabela `produto`, com filtro por restaurante.

| Método | Retorno | Descrição |
|---|---|---|
| `inserir(Produto p)` | `void` | INSERT com `RETURNING id` |
| `listarTodos()` | `List<Produto>` | SELECT todos os produtos ordenados por id |
| `listarPorRestaurante(int id)` | `List<Produto>` | SELECT WHERE `restaurante_id = ?` — cardápio de um restaurante |
| `buscarPorId(int id)` | `Optional<Produto>` | SELECT por PK |
| `atualizar(Produto p)` | `boolean` | UPDATE de todos os campos |
| `excluir(int id)` | `boolean` | DELETE por id |

---

### 5.4 RestauranteDAO

Gerencia operações CRUD da tabela `restaurante`, com relatório de vendas agregado.

| Método | Retorno | Descrição |
|---|---|---|
| `inserir(Restaurante r)` | `void` | INSERT com `RETURNING id` |
| `listarTodos()` | `List<Restaurante>` | SELECT todos os restaurantes ordenados por id |
| `buscarPorId(int id)` | `Optional<Restaurante>` | SELECT por PK |
| `atualizar(Restaurante r)` | `boolean` | UPDATE de todos os campos |
| `excluir(int id)` | `boolean` | DELETE por id |
| `relatorioVendas(PedidoDAO dao)` | `void` | Exibe no console total de pedidos e faturamento por restaurante |

---

### 5.5 PedidoDAO

DAO mais completo do sistema. Gerencia o ciclo de vida dos pedidos com múltiplos filtros e atualização de status.

| Método | Retorno | Descrição |
|---|---|---|
| `inserir(Pedido p)` | `void` | INSERT completo com `RETURNING id` — aceita entregador nulo (`setNull`) |
| `listarTodos()` | `List<Pedido>` | SELECT todos os pedidos |
| `listarPorCliente(int id)` | `List<Pedido>` | Todos os pedidos de um cliente específico |
| `listarPorRestaurante(int id)` | `List<Pedido>` | Todos os pedidos de um restaurante específico |
| `buscarPorId(int id)` | `Optional<Pedido>` | SELECT por PK |
| `atualizarStatus(int id, StatusPedido status)` | `boolean` | UPDATE apenas do campo `status` — para avançar no fluxo |
| `atualizar(Pedido p)` | `boolean` | UPDATE completo de todos os campos |
| `excluir(int id)` | `boolean` | DELETE por id |

**Entregador nulo:** o campo `entregador_id` é nullable no banco e tipado como `Integer` (wrapper) no Java. O DAO usa `ps.setNull(3, Types.INTEGER)` ao inserir/atualizar com valor nulo, e `rs.wasNull()` no mapeamento para detectar `NULL` retornado pelo banco.

**Padrão interno `PsSetter`:** interface funcional privada que permite reaproveitar o método `buscarLista()` com diferentes cláusulas `WHERE`, sem duplicar o tratamento de `ResultSet` e conexão.

---

### 5.6 ItemPedidoDAO

Gerencia os itens associados a cada pedido. Não há UPDATE de item — para alterar um pedido, os itens são excluídos e recriados.

| Método | Retorno | Descrição |
|---|---|---|
| `inserir(ItemPedido item)` | `void` | INSERT com `RETURNING id` — usa `BigDecimal` para `preco_unitario` |
| `listarPorPedido(int id)` | `List<ItemPedido>` | Todos os itens de um pedido — principal query de leitura |
| `listarTodos()` | `List<ItemPedido>` | SELECT todos os itens do sistema ordenados por id |
| `buscarPorId(int id)` | `Optional<ItemPedido>` | SELECT por PK |
| `excluirPorPedido(int id)` | `void` | DELETE WHERE `pedido_id = ?` — remove todos os itens de um pedido |

---

## 6. Tratamento de Erros

Todos os DAOs seguem o mesmo padrão: a `SQLException` (checked) é capturada e relançada como `RuntimeException` (unchecked) com uma mensagem descritiva e a exceção original como causa.

```java
} catch (SQLException e) {
    throw new RuntimeException("Erro ao inserir cliente", e);
}
```

**Vantagens do padrão:**
- A camada de serviço não precisa declarar `throws SQLException`
- A stack trace completa é preservada na causa da exceção
- Mensagens padronizadas facilitam o diagnóstico em logs

### Erros Comuns e Causas

| Mensagem | Origem | Causa Provável |
|---|---|---|
| `Connection refused` | HikariCP | PostgreSQL não está rodando ou URL/porta incorretos |
| `password authentication failed` | HikariCP | `DB_USER` ou `DB_PASSWORD` incorretos |
| `relation does not exist` | SQL | `query.sql` não foi executado — tabelas não criadas |
| `duplicate key value` | SQL | Tentativa de inserir e-mail de cliente já existente (UNIQUE) |
| `violates check constraint` | SQL | Status inválido para pedido ou entregador |
| `violates foreign key constraint` | SQL | Referência a ID inexistente ou tentativa de deletar pai com filhos |

---

## 7. Configuração Inicial do Ambiente

### 7.1 Pré-requisitos

- PostgreSQL instalado e rodando na porta 5432
- Java 17 ou superior
- Maven configurado (`pom.xml` na raiz do projeto)

### 7.2 Criar o banco de dados

```sql
-- No psql ou pgAdmin:
CREATE DATABASE delivery_food;
```

### 7.3 Criar as tabelas

```bash
# Executar o arquivo query.sql na raiz do projeto:
psql -U postgres -d delivery_food -f query.sql
```

### 7.4 Ordem de criação das tabelas

Respeitar a ordem abaixo para não violar Foreign Keys:

| Ordem | Tabela | Dependências |
|---|---|---|
| 1° | `restaurante` | Nenhuma |
| 2° | `produto` | `restaurante` |
| 3° | `cliente` | Nenhuma |
| 4° | `entregador` | Nenhuma |
| 5° | `pedido` | `cliente`, `restaurante`, `entregador` |
| 6° | `item_pedido` | `pedido`, `produto` |

### 7.5 Credenciais padrão

As credenciais estão definidas em `DatabaseConnection.java`. Em desenvolvimento, os valores padrão são:

```
URL:     jdbc:postgresql://localhost:5432/delivery_food
Usuário: postgres
Senha:   admin
```

Para alterar sem editar o código, defina as variáveis de ambiente antes de executar a aplicação:

```bash
# Linux/macOS
export DB_URL=jdbc:postgresql://localhost:5432/delivery_food
export DB_USER=postgres
export DB_PASSWORD=sua_senha

# Windows (PowerShell)
$env:DB_URL="jdbc:postgresql://localhost:5432/delivery_food"
$env:DB_USER="postgres"
$env:DB_PASSWORD="sua_senha"
```
