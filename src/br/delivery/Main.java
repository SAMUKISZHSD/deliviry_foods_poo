package br.delivery;
 
import br.delivery.dao.*;
import br.delivery.model.*;
import br.delivery.service.CalculadorPedido;
 
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
public class Main {

    // -----------------------------------------------------------------------
    // DAOs e Services
    // -----------------------------------------------------------------------
    private static final DatabaseConnection db = DatabaseConnection.getInstance();
    private static final RestauranteDAO restauranteDAO = new RestauranteDAO(db);
    private static final ProdutoDAO produtoDAO = new ProdutoDAO(db);
    private static final ClienteDAO clienteDAO = new ClienteDAO(db);
    private static final EntregadorDAO entregadorDAO = new EntregadorDAO(db);
    private static final PedidoDAO pedidoDAO = new PedidoDAO(db);
    private static final ItemPedidoDAO itemPedidoDAO = new ItemPedidoDAO(db);
    private static final CalculadorPedido calculador = new CalculadorPedido();

    private static final Scanner sc = new Scanner(System.in);

    // -----------------------------------------------------------------------
    // Main
    // -----------------------------------------------------------------------
    public static void main(String[] args) {
        popularDadosIniciais();
        menuPrincipal();
    }

    // -----------------------------------------------------------------------
    // Menu do Sistema
    // -----------------------------------------------------------------------
    private static void menuPrincipal() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║         SISTEMA DE DELIVERY          ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║  1. Restaurantes                     ║");
            System.out.println("║  2. Produtos                         ║");
            System.out.println("║  3. Clientes                         ║");
            System.out.println("║  4. Entregadores                     ║");
            System.out.println("║  5. Pedidos                          ║");
            System.out.println("║  6. Relatório de Vendas              ║");
            System.out.println("║  0. Sair                             ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Opção: ");
            int op = lerInt();
            switch (op) {
                case 1 -> menuRestaurantes();
                case 2 -> menuProdutos();
                case 3 -> menuClientes();
                case 4 -> menuEntregadores();
                case 5 -> menuPedidos();
                case 6 -> restauranteDAO.relatorioVendas(pedidoDAO);
                case 0 -> { System.out.println("Até logo!"); return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Restaurantes
    // -----------------------------------------------------------------------
    private static void menuRestaurantes() {
        while (true) {
            System.out.println("\n--- RESTAURANTES ---");
            System.out.println("1. Listar  2. Cadastrar  3. Atualizar  4. Excluir  0. Voltar");
            System.out.print("Opção: ");
            switch (lerInt()) {
                case 1 -> listarRestaurantes();
                case 2 -> cadastrarRestaurante();
                case 3 -> atualizarRestaurante();
                case 4 -> excluirRestaurante();
                case 0 -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void listarRestaurantes() {
        List<Restaurante> lista = restauranteDAO.listarTodos();
        if (lista.isEmpty()) { System.out.println("Nenhum restaurante cadastrado."); return; }
        lista.forEach(System.out::println);
    }

    private static void cadastrarRestaurante() {
        System.out.print("Nome: "); String nome = sc.nextLine();
        System.out.print("Endereço: "); String end = sc.nextLine();
        System.out.print("Telefone: "); String tel = sc.nextLine();
        System.out.print("Categoria (ex: Pizzaria, Japonesa): "); String cat = sc.nextLine();
        restauranteDAO.inserir(new Restaurante(nome, end, tel, cat));
    }

    private static void atualizarRestaurante() {
        listarRestaurantes();
        System.out.print("ID do restaurante a atualizar: ");
        int id = lerInt();
        Optional<Restaurante> opt = restauranteDAO.buscarPorId(id);
        if (opt.isEmpty()) { System.out.println("Não encontrado."); return; }
        Restaurante r = opt.get();
        System.out.print("Novo nome [" + r.getNome() + "]: "); String nome = sc.nextLine();
        System.out.print("Novo endereço [" + r.getEndereco() + "]: "); String end = sc.nextLine();
        System.out.print("Novo telefone [" + r.getTelefone() + "]: "); String tel = sc.nextLine();
        System.out.print("Nova categoria [" + r.getCategoria() + "]: "); String cat = sc.nextLine();
        if (!nome.isBlank()) r.setNome(nome);
        if (!end.isBlank()) r.setEndereco(end);
        if (!tel.isBlank()) r.setTelefone(tel);
        if (!cat.isBlank()) r.setCategoria(cat);
        restauranteDAO.atualizar(r);
        System.out.println("Atualizado.");
    }

    private static void excluirRestaurante() {
        listarRestaurantes();
        System.out.print("ID a excluir: ");
        System.out.println(restauranteDAO.excluir(lerInt()) ? "Excluído." : "Não encontrado.");
    }

    // -----------------------------------------------------------------------
    // Produtos
    // -----------------------------------------------------------------------
    private static void menuProdutos() {
        while (true) {
            System.out.println("\n--- PRODUTOS ---");
            System.out.println("1. Listar todos  2. Listar por restaurante  3. Cadastrar  4. Atualizar  5. Excluir  0. Voltar");
            System.out.print("Opção: ");
            switch (lerInt()) {
                case 1 -> produtoDAO.listarTodos().forEach(System.out::println);
                case 2 -> listarProdutosPorRestaurante();
                case 3 -> cadastrarProduto();
                case 4 -> atualizarProduto();
                case 5 -> excluirProduto();
                case 0 -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void listarProdutosPorRestaurante() {
        listarRestaurantes();
        System.out.print("ID do restaurante: ");
        int rid = lerInt();
        List<Produto> lista = produtoDAO.listarPorRestaurante(rid);
        if (lista.isEmpty()) System.out.println("Sem produtos.");
        else lista.forEach(System.out::println);
    }

    private static void cadastrarProduto() {
        listarRestaurantes();
        System.out.print("ID do restaurante: "); int rid = lerInt();
        if (restauranteDAO.buscarPorId(rid).isEmpty()) { System.out.println("Restaurante não encontrado."); return; }
        System.out.print("Nome: "); String nome = sc.nextLine();
        System.out.print("Descrição: "); String desc = sc.nextLine();
        System.out.print("Preço (ex: 32.90): "); double preco = lerDouble();
        produtoDAO.inserir(new Produto(rid, nome, desc, preco));
    }

    private static void atualizarProduto() {
        produtoDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID do produto: "); int id = lerInt();
        Optional<Produto> opt = produtoDAO.buscarPorId(id);
        if (opt.isEmpty()) { System.out.println("Não encontrado."); return; }
        Produto p = opt.get();
        System.out.print("Novo nome [" + p.getNome() + "]: "); String nome = sc.nextLine();
        System.out.print("Nova descrição [" + p.getDescricao() + "]: "); String desc = sc.nextLine();
        System.out.print("Novo preço [" + p.getPreco() + "] (0 = manter): "); double preco = lerDouble();
        if (!nome.isBlank()) p.setNome(nome);
        if (!desc.isBlank()) p.setDescricao(desc);
        if (preco > 0) p.setPreco(preco);
        produtoDAO.atualizar(p);
        System.out.println("Atualizado.");
    }

    private static void excluirProduto() {
        produtoDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID a excluir: ");
        System.out.println(produtoDAO.excluir(lerInt()) ? "Excluído." : "Não encontrado.");
    }

    // -----------------------------------------------------------------------
    // Clientes
    // -----------------------------------------------------------------------
    private static void menuClientes() {
        while (true) {
            System.out.println("\n--- CLIENTES ---");
            System.out.println("1. Listar  2. Cadastrar  3. Atualizar  4. Excluir  0. Voltar");
            System.out.print("Opção: ");
            switch (lerInt()) {
                case 1 -> clienteDAO.listarTodos().forEach(System.out::println);
                case 2 -> cadastrarCliente();
                case 3 -> atualizarCliente();
                case 4 -> excluirCliente();
                case 0 -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void cadastrarCliente() {
        System.out.print("Nome: "); String nome = sc.nextLine();
        System.out.print("E-mail: "); String email = sc.nextLine();
        System.out.print("Telefone: "); String tel = sc.nextLine();
        System.out.print("Endereço de entrega: "); String end = sc.nextLine();
        clienteDAO.inserir(new Cliente(nome, email, tel, end));
    }

    private static void atualizarCliente() {
        clienteDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID: "); int id = lerInt();
        Optional<Cliente> opt = clienteDAO.buscarPorId(id);
        if (opt.isEmpty()) { System.out.println("Não encontrado."); return; }
        Cliente c = opt.get();
        System.out.print("Novo nome [" + c.getNome() + "]: "); String nome = sc.nextLine();
        System.out.print("Novo e-mail [" + c.getEmail() + "]: "); String email = sc.nextLine();
        System.out.print("Novo telefone [" + c.getTelefone() + "]: "); String tel = sc.nextLine();
        System.out.print("Novo endereço [" + c.getEndereco() + "]: "); String end = sc.nextLine();
        if (!nome.isBlank()) c.setNome(nome);
        if (!email.isBlank()) c.setEmail(email);
        if (!tel.isBlank()) c.setTelefone(tel);
        if (!end.isBlank()) c.setEndereco(end);
        clienteDAO.atualizar(c);
        System.out.println("Atualizado.");
    }

    private static void excluirCliente() {
        clienteDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID a excluir: ");
        System.out.println(clienteDAO.excluir(lerInt()) ? "Excluído." : "Não encontrado.");
    }

    // -----------------------------------------------------------------------
    // Entregadores
    // -----------------------------------------------------------------------
    private static void menuEntregadores() {
        while (true) {
            System.out.println("\n--- ENTREGADORES ---");
            System.out.println("1. Listar todos  2. Listar disponíveis  3. Cadastrar  4. Atualizar  5. Alterar status  6. Excluir  0. Voltar");
            System.out.print("Opção: ");
            switch (lerInt()) {
                case 1 -> entregadorDAO.listarTodos().forEach(System.out::println);
                case 2 -> entregadorDAO.listarDisponiveis().forEach(System.out::println);
                case 3 -> cadastrarEntregador();
                case 4 -> atualizarEntregador();
                case 5 -> alterarStatusEntregador();
                case 6 -> excluirEntregador();
                case 0 -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void cadastrarEntregador() {
        System.out.print("Nome: "); String nome = sc.nextLine();
        System.out.print("Telefone: "); String tel = sc.nextLine();
        System.out.print("Veículo (ex: Moto, Bicicleta): "); String vei = sc.nextLine();
        entregadorDAO.inserir(new Entregador(nome, tel, vei));
    }

    private static void atualizarEntregador() {
        entregadorDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID: "); int id = lerInt();
        Optional<Entregador> opt = entregadorDAO.buscarPorId(id);
        if (opt.isEmpty()) { System.out.println("Não encontrado."); return; }
        Entregador e = opt.get();
        System.out.print("Novo nome [" + e.getNome() + "]: "); String nome = sc.nextLine();
        System.out.print("Novo telefone [" + e.getTelefone() + "]: "); String tel = sc.nextLine();
        System.out.print("Novo veículo [" + e.getVeiculo() + "]: "); String vei = sc.nextLine();
        if (!nome.isBlank()) e.setNome(nome);
        if (!tel.isBlank()) e.setTelefone(tel);
        if (!vei.isBlank()) e.setVeiculo(vei);
        entregadorDAO.atualizar(e);
        System.out.println("Atualizado.");
    }

    private static void alterarStatusEntregador() {
        entregadorDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID do entregador: "); int id = lerInt();
        Optional<Entregador> opt = entregadorDAO.buscarPorId(id);
        if (opt.isEmpty()) { System.out.println("Não encontrado."); return; }
        Entregador e = opt.get();
        System.out.println("Novo status: 1=DISPONÍVEL  2=EM_ENTREGA  3=INATIVO");
        System.out.print("Opção: ");
        switch (lerInt()) {
            case 1 -> e.setStatus(StatusEntregador.DISPONIVEL);
            case 2 -> e.setStatus(StatusEntregador.EM_ENTREGA);
            case 3 -> e.setStatus(StatusEntregador.INATIVO);
            default -> { System.out.println("Opção inválida."); return; }
        }
        entregadorDAO.atualizar(e);
        System.out.println("Status atualizado para: " + e.getStatus().getDescricao());
    }

    private static void excluirEntregador() {
        entregadorDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID a excluir: ");
        System.out.println(entregadorDAO.excluir(lerInt()) ? "Excluído." : "Não encontrado.");
    }

    // -----------------------------------------------------------------------
    // Pedidos
    // -----------------------------------------------------------------------
    private static void menuPedidos() {
        while (true) {
            System.out.println("\n--- PEDIDOS ---");
            System.out.println("1. Listar todos  2. Detalhar pedido  3. Criar pedido  4. Atribuir entregador  5. Atualizar status  6. Excluir  0. Voltar");
            System.out.print("Opção: ");
            switch (lerInt()) {
                case 1 -> pedidoDAO.listarTodos().forEach(System.out::println);
                case 2 -> detalharPedido();
                case 3 -> criarPedido();
                case 4 -> atribuirEntregador();
                case 5 -> atualizarStatusPedido();
                case 6 -> excluirPedido();
                case 0 -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void detalharPedido() {
        pedidoDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID do pedido: "); int id = lerInt();
        Optional<Pedido> opt = pedidoDAO.buscarPorId(id);
        if (opt.isEmpty()) { System.out.println("Não encontrado."); return; }
        Pedido p = opt.get();
        System.out.println("\n--- DETALHES DO PEDIDO #" + p.getId() + " ---");
        System.out.println(p);
        List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(p.getId());
        System.out.println("Itens:");
        itens.forEach(i -> System.out.println("  " + i));
        System.out.println(calculador.gerarResumo(p));
    }

    private static void criarPedido() {
        // 1. Escolhe cliente
        System.out.println("\n== CLIENTES ==");
        clienteDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID do cliente: "); int cid = lerInt();
        if (clienteDAO.buscarPorId(cid).isEmpty()) { System.out.println("Cliente não encontrado."); return; }

        // 2. Escolhe restaurante
        System.out.println("\n== RESTAURANTES ==");
        listarRestaurantes();
        System.out.print("ID do restaurante: "); int rid = lerInt();
        if (restauranteDAO.buscarPorId(rid).isEmpty()) { System.out.println("Restaurante não encontrado."); return; }

        // 3. Cria pedido provisório
        Pedido pedido = new Pedido(cid, rid);
        pedidoDAO.inserir(pedido);

        // 4. Adiciona itens
        List<Produto> produtos = produtoDAO.listarPorRestaurante(rid);
        if (produtos.isEmpty()) {
            System.out.println("Este restaurante não tem produtos cadastrados.");
            pedidoDAO.excluir(pedido.getId());
            return;
        }

        boolean adicionando = true;
        while (adicionando) {
            System.out.println("\n== PRODUTOS DO RESTAURANTE ==");
            produtos.forEach(System.out::println);
            System.out.print("ID do produto (0 para finalizar): "); int pid = lerInt();
            if (pid == 0) break;

            Optional<Produto> optProd = produtoDAO.buscarPorId(pid);
            if (optProd.isEmpty() || optProd.get().getRestauranteId() != rid) {
                System.out.println("Produto inválido para este restaurante.");
                continue;
            }
            Produto prod = optProd.get();
            System.out.print("Quantidade: "); int qtd = lerInt();
            if (qtd <= 0) { System.out.println("Quantidade inválida."); continue; }

            itemPedidoDAO.inserir(new ItemPedido(pedido.getId(), prod.getId(), qtd, prod.getPreco()));
            System.out.printf("✔ %dx %s adicionado.%n", qtd, prod.getNome());
        }

        // 5. Calcula valores
        List<ItemPedido> itens = itemPedidoDAO.listarPorPedido(pedido.getId());
        if (itens.isEmpty()) {
            System.out.println("Nenhum item adicionado. Pedido cancelado.");
            pedidoDAO.excluir(pedido.getId());
            return;
        }

        calculador.calcular(pedido, itens);
        pedidoDAO.atualizar(pedido);

        System.out.println(calculador.gerarResumo(pedido));
    }

    private static void atribuirEntregador() {
        // Regra de negócio: entregador precisa estar DISPONIVEL
        pedidoDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID do pedido: "); int pid = lerInt();
        Optional<Pedido> optP = pedidoDAO.buscarPorId(pid);
        if (optP.isEmpty()) { System.out.println("Pedido não encontrado."); return; }
        Pedido pedido = optP.get();

        System.out.println("\n== ENTREGADORES DISPONÍVEIS ==");
        List<Entregador> disponiveis = entregadorDAO.listarDisponiveis();
        if (disponiveis.isEmpty()) { System.out.println("Nenhum entregador disponível no momento."); return; }
        disponiveis.forEach(System.out::println);

        System.out.print("ID do entregador: "); int eid = lerInt();
        Optional<Entregador> optE = entregadorDAO.buscarPorId(eid);
        if (optE.isEmpty()) { System.out.println("Entregador não encontrado."); return; }
        Entregador entregador = optE.get();

        if (entregador.getStatus() != StatusEntregador.DISPONIVEL) {
            System.out.println("✘ Entregador não está disponível. Status atual: " + entregador.getStatus().getDescricao());
            return;
        }

        // Atribui e muda status
        pedido.setEntregadorId(entregador.getId());
        pedido.setStatus(StatusPedido.SAIU_ENTREGA);
        pedidoDAO.atualizar(pedido);

        entregador.setStatus(StatusEntregador.EM_ENTREGA);
        entregadorDAO.atualizar(entregador);

        System.out.printf("✔ Entregador '%s' atribuído ao pedido #%d. Status do pedido: %s%n",
                entregador.getNome(), pedido.getId(), pedido.getStatus().getDescricao());
    }

    private static void atualizarStatusPedido() {
        pedidoDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID do pedido: "); int pid = lerInt();
        Optional<Pedido> opt = pedidoDAO.buscarPorId(pid);
        if (opt.isEmpty()) { System.out.println("Pedido não encontrado."); return; }

        System.out.println("Novo status:");
        StatusPedido[] statuses = StatusPedido.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, statuses[i].getDescricao());
        }
        System.out.print("Opção: "); int op = lerInt();
        if (op < 1 || op > statuses.length) { System.out.println("Opção inválida."); return; }

        StatusPedido novoStatus = statuses[op - 1];
        Pedido pedido = opt.get();

        // Se entregue, libera o entregador
        if (novoStatus == StatusPedido.ENTREGUE && pedido.getEntregadorId() != null) {
            entregadorDAO.buscarPorId(pedido.getEntregadorId()).ifPresent(e -> {
                e.setStatus(StatusEntregador.DISPONIVEL);
                entregadorDAO.atualizar(e);
                System.out.println("✔ Entregador '" + e.getNome() + "' voltou a ficar disponível.");
            });
        }

        pedidoDAO.atualizarStatus(pedido.getId(), novoStatus);
        System.out.println("✔ Status atualizado para: " + novoStatus.getDescricao());
    }

    private static void excluirPedido() {
        pedidoDAO.listarTodos().forEach(System.out::println);
        System.out.print("ID a excluir: "); int id = lerInt();
        itemPedidoDAO.excluirPorPedido(id);
        System.out.println(pedidoDAO.excluir(id) ? "Pedido e itens excluídos." : "Não encontrado.");
    }

    // -----------------------------------------------------------------------
    // Dados ficticios para testes MVP do sistema
    // -----------------------------------------------------------------------
    private static void popularDadosIniciais() {

        // Restaurantes
        restauranteDAO.inserir(new Restaurante("Pizzaria Bella", "Rua das Flores, 10", "11 9999-1111", "Pizzaria"));
        restauranteDAO.inserir(new Restaurante("Sushi do Mestre", "Av. Japão, 55", "11 9999-2222", "Japonesa"));

        // Produtos
        produtoDAO.inserir(new Produto(1, "Pizza Margherita", "Molho, mussarela e manjericão", 45.90));
        produtoDAO.inserir(new Produto(1, "Pizza Calabresa", "Molho, calabresa e cebola", 49.90));
        produtoDAO.inserir(new Produto(1, "Refrigerante 2L", "Coca-Cola ou Guaraná", 12.00));
        produtoDAO.inserir(new Produto(2, "Combo Sashimi 20 peças", "Salmão e atum", 89.90));
        produtoDAO.inserir(new Produto(2, "Temaki Salmão", "Cone de arroz com salmão e cream cheese", 28.50));

        // Clientes
        clienteDAO.inserir(new Cliente("Ana Souza", "ana@email.com", "11 91111-0001", "Rua Verde, 321 - Ap 5"));
        clienteDAO.inserir(new Cliente("Carlos Lima", "carlos@email.com", "11 92222-0002", "Av. Central, 1000"));

        // Entregadores
        entregadorDAO.inserir(new Entregador("João Moto", "11 93333-0001", "Moto"));
        entregadorDAO.inserir(new Entregador("Maria Bike", "11 94444-0002", "Bicicleta"));
    }

    // -----------------------------------------------------------------------
    // Utilitários de leitura segura - Umas caprichos a mais
    // -----------------------------------------------------------------------
    private static int lerInt() {
        while (true) {
            try {
                String linha = sc.nextLine().trim();
                return Integer.parseInt(linha);
            } catch (NumberFormatException e) {
                System.out.print("Digite um número inteiro válido: ");
            }
        }
    }

    private static double lerDouble() {
        while (true) {
            try {
                String linha = sc.nextLine().trim().replace(",", ".");
                return Double.parseDouble(linha);
            } catch (NumberFormatException e) {
                System.out.print("Digite um valor numérico válido (ex: 32.90): ");
            }
        }
    }
}
