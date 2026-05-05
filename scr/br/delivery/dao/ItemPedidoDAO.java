

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ItemPedido;

public class ItemPedidoDAO {

    private final DatabaseConnection db;
    private final List<ItemPedido> tabela = new ArrayList<>();
    private int proximoId = 1;

    public ItemPedidoDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(ItemPedido item) {
        item.setId(proximoId++);
        tabela.add(item);
    }

    public List<ItemPedido> listarPorPedido(int pedidoId) {
        return tabela.stream()
                .filter(i -> i.getPedidoId() == pedidoId)
                .collect(Collectors.toList());
    }

    public Optional<ItemPedido> buscarPorId(int id) {
        return tabela.stream().filter(i -> i.getId() == id).findFirst();
    }

    /** Remove todos os itens de um pedido (ex: ao cancelar/excluir pedido). */
    public void excluirPorPedido(int pedidoId) {
        tabela.removeIf(i -> i.getPedidoId() == pedidoId);
    }

    public List<ItemPedido> listarTodos() {
        return new ArrayList<>(tabela);
    }
}