

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import Pedido;
import StatusPedido;

public class PedidoDAO {

    private final DatabaseConnection db;
    private final List<Pedido> tabela = new ArrayList<>();
    private int proximoId = 1;

    public PedidoDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Pedido p) {
        p.setId(proximoId++);
        tabela.add(p);
        System.out.println("✔ Pedido #" + p.getId() + " criado com sucesso.");
    }

    public List<Pedido> listarTodos() {
        return new ArrayList<>(tabela);
    }

    public List<Pedido> listarPorRestaurante(int restauranteId) {
        return tabela.stream()
                .filter(p -> p.getRestauranteId() == restauranteId)
                .collect(Collectors.toList());
    }

    public List<Pedido> listarPorCliente(int clienteId) {
        return tabela.stream()
                .filter(p -> p.getClienteId() == clienteId)
                .collect(Collectors.toList());
    }

    public Optional<Pedido> buscarPorId(int id) {
        return tabela.stream().filter(p -> p.getId() == id).findFirst();
    }

    public boolean atualizarStatus(int pedidoId, StatusPedido novoStatus) {
        Optional<Pedido> opt = buscarPorId(pedidoId);
        if (opt.isPresent()) {
            opt.get().setStatus(novoStatus);
            return true;
        }
        return false;
    }

    public boolean atualizar(Pedido atualizado) {
        for (int i = 0; i < tabela.size(); i++) {
            if (tabela.get(i).getId() == atualizado.getId()) {
                tabela.set(i, atualizado);
                return true;
            }
        }
        return false;
    }

    public boolean excluir(int id) {
        return tabela.removeIf(p -> p.getId() == id);
    }
}