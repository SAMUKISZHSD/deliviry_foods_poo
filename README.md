# Delivery Foods POO

## Visão Geral

Projeto Java Maven para um sistema de delivery de alimentos em CLI
O sistema gerencia restaurantes, produtos, clientes, entregadores e pedidos, com persistência em PostgreSQL.

## Estrutura do Projeto

- `pom.xml` — configuração Maven do projeto
- `src/` — código-fonte principal em pacote `br.delivery`
- `src/br/delivery/Main.java` — classe principal que exibe menus e controla o fluxo do sistema
- `src/br/delivery/dao/` — classes de acesso a dados com JDBC e HikariCP
- `src/br/delivery/model/` — classes de modelo de domínio e enums de status
- `src/br/delivery/service/` — lógica de negócio e cálculos de pedido

## Tecnologias

- Java 17 (configurado atualmente no Maven)
- Maven
- PostgreSQL JDBC (`org.postgresql:postgresql:42.7.3`)
- HikariCP (`com.zaxxer:HikariCP:5.1.0`)
- Maven Compiler Plugin (`3.11.0`)
- Maven Assembly Plugin para criar JAR executável com dependências

## Principais Componentes

### `Main.java`
- Menu interativo para o usuário
- Validação de conexão com o banco
- CRUD de restaurantes, produtos, clientes, entregadores e pedidos
- Geração de relatório de vendas

### `DatabaseConnection.java`
- Singleton que configura o pool HikariCP
- Conexão com PostgreSQL via variáveis de ambiente ou valores padrão
- Teste de conexão e encerramento seguro do pool

### DAOs
- `RestauranteDAO`, `ProdutoDAO`, `ClienteDAO`, `EntregadorDAO`, `PedidoDAO`, `ItemPedidoDAO`
- Implementam operações básicas de CRUD e listagens específicas
- Utilizam `PreparedStatement` e `try-with-resources`

### Modelos
- `Pedido`, `Cliente`, `Restaurante`, `Produto`, `Entregador`, `ItemPedido`
- Enums `StatusPedido` e `StatusEntregador`
- `Pedido` armazena subtotal, desconto, taxa de entrega, valor final e data de criação

### Lógica de Pedido
- `CalculadorPedido` calcula subtotal, desconto progressivo e valor final
- Taxa de entrega fixa de R$ 8,00
- Resumo do pedido formatado em texto

## Observações para Upgrade

- O projeto atualmente usa Java 17.
- Solicitação atual: atualizar para a versão LTS mais recente (Java 21 ou superior, dependendo do momento).
- A migração exigirá ajuste de `maven.compiler.source` e `maven.compiler.target`, e possivelmente revisão de dependências.

## Como Executar

1. Configure as variáveis de ambiente do PostgreSQL:
   - `DB_URL`
   - `DB_USER`
   - `DB_PASSWORD`
2. Execute com Maven:
   - `mvn clean package`
   - `java -jar target/delivery-system-1.0-SNAPSHOT-jar-with-dependencies.jar`
