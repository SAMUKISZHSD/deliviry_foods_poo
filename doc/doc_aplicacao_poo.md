# POO no Projeto Delivery Foods

## 1. Visão Geral

O projeto aplica conceitos básicos de Programação Orientada a Objetos (POO) em uma implementação Java para um sistema de delivery.
Ele organiza o código em classes, abstrai responsabilidade em componentes distintos e usa encapsulamento para proteção de dados.

## 2. Classes e Objetos

O projeto é estruturado em classes que representam entidades do domínio e partes da aplicação:

- `Main` — classe de entrada do programa e controlador do fluxo de interface de usuário.
- `DatabaseConnection` — gerencia o pool de conexões com o PostgreSQL.
- DAOs (`RestauranteDAO`, `ProdutoDAO`, `ClienteDAO`, `EntregadorDAO`, `PedidoDAO`, `ItemPedidoDAO`) — implementam acesso a dados e operações CRUD.
- Modelos (`Restaurante`, `Produto`, `Cliente`, `Entregador`, `Pedido`, `ItemPedido`) — representam os dados do domínio.
- `CalculadorPedido` — encapsula lógica de cálculo de valores do pedido.
- Enums (`StatusPedido`, `StatusEntregador`) — representam estados fixos com significado semântico.

Cada instância dessas classes representa um objeto com estado próprio e comportamento definido.

## 3. Encapsulamento

O projeto usa encapsulamento para proteger os dados:

- Classes de modelo expõem campos privados e métodos públicos `get`/`set`.
- A conexão com o banco é encapsulada em `DatabaseConnection`, que controla a criação, uso e fechamento do pool HikariCP.
- A classe `CalculadorPedido` mantém `TAXA_ENTREGA` como constante privada e fornece métodos públicos para calcular subtotal, desconto e valor final.

Encapsulamento aqui ajuda a manter regras internas isoladas e a evitar acesso direto aos campos.

## 4. Abstração

Abstração aparece ao separar responsabilidades em classes específicas:

- DAOs abstraem detalhes de SQL, conexões e mapeamento `ResultSet`/`PreparedStatement`.
- `CalculadorPedido` abstrai a regra de cálculo dos pedidos, deixando a classe `Main` mais simples.
- Models abstraem os dados do domínio e centralizam a representação de entidades.

Isso permite trabalhar com conceitos de negócio (pedido, cliente, restaurante) sem duplicar lógica de acesso a dados ou cálculos.

## 5. Coesão e Separação de Responsabilidades

O projeto demonstra coesão e separação de responsabilidades em várias camadas:

- `Main`: interface de usuário e fluxo do sistema.
- `DAO`: persistência de dados e operações no banco.
- `Model`: estrutura dos dados.
- `Service`: regras de cálculo e lógica de negócio.

Cada classe tem responsabilidade bem definida, o que é um princípio central da POO.

## 6. Polimorfismo e Herança

Este projeto não faz uso significativo de herança ou polimorfismo explícito.

- Não há classes base comuns estendidas por outras classes.
- Não há uso de interfaces para variáveis polimórficas.

Isso não impede a aplicação de POO, mas indica uma arquitetura mais simples e orientada a classes concretas.

## 7. Uso de Enumerações

Os enums `StatusPedido` e `StatusEntregador` representam conjuntos fixos de estados.

- Eles tornam o código mais legível e seguro do que strings soltas.
- Cada enum encapsula uma descrição legível (`getDescricao()`), o que é uma forma de associar dados a um tipo enumerado.

## 8. Conclusão

O projeto aplica os pilares básicos de POO:

- classes como unidades de encapsulamento,
- abstração de persistência e cálculo,
- coesão entre responsabilidades,
- uso de objetos e enums para representar o domínio.

A arquitetura atual é simples e adequada para uma aplicação CRUD em linha de comando.

Para evoluir a aplicação em termos de POO, pode-se adicionar interfaces, camadas de serviço mais claras ou herança onde houver comportamento comum entre classes.
