package br.delivery.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.delivery.model.*;

public class RestauranteDAO {

    private final DatabaseConnection db;
    // Banco fictício: lista em memória
    private final List<Restaurante> tabela = new ArrayList<>();
    private int proximoId = 1;

    public RestauranteDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Restaurante r) {
        r.setId(proximoId++);
        tabela.add(r);
        System.out.println("✔ Restaurante '" + r.getNome() + "' cadastrado com ID " + r.getId());
    }

    public List<Restaurante> listarTodos() {
        return new ArrayList<>(tabela);
    }

    public Optional<Restaurante> buscarPorId(int id) {
        return tabela.stream().filter(r -> r.getId() == id).findFirst();
    }

    public boolean atualizar(Restaurante atualizado) {
        for (int i = 0; i < tabela.size(); i++) {
            if (tabela.get(i).getId() == atualizado.getId()) {
                tabela.set(i, atualizado);
                return true;
            }
        }
        return false;
    }

    public boolean excluir(int id) {
        return tabela.removeIf(r -> r.getId() == id);
    }

    /** Relatório: total de pedidos e valor vendido por restaurante. */
    public void relatorioVendas(PedidoDAO pedidoDAO) {
        System.out.println("\n======= RELATÓRIO DE VENDAS POR RESTAURANTE =======");
        for (Restaurante r : tabela) {
            List<Pedido> pedidos = pedidoDAO.listarPorRestaurante(r.getId());
            double total = pedidos.stream()
                    .mapToDouble(Pedido::getValorFinal)
                    .sum();
            System.out.printf("  %-25s | Pedidos: %3d | Total: R$ %8.2f%n",
                    r.getNome(), pedidos.size(), total);
        }
        System.out.println("====================================================\n");
    }
}