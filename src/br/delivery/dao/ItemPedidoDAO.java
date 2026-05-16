package br.delivery.dao;

import br.delivery.model.ItemPedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemPedidoDAO {

    private final DatabaseConnection db;

    public ItemPedidoDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(ItemPedido item) {
        String sql = """
            INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario)
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getPedidoId());
            ps.setInt(2, item.getProdutoId());
            ps.setInt(3, item.getQuantidade());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(item.getPrecoUnitario()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) item.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir item de pedido", e);
        }
    }

    public List<ItemPedido> listarPorPedido(int pedidoId) {
        String sql = "SELECT id, pedido_id, produto_id, quantidade, preco_unitario FROM item_pedido WHERE pedido_id = ?";
        List<ItemPedido> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pedidoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar itens do pedido", e);
        }
        return lista;
    }

    public Optional<ItemPedido> buscarPorId(int id) {
        String sql = "SELECT id, pedido_id, produto_id, quantidade, preco_unitario FROM item_pedido WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar item de pedido", e);
        }
        return Optional.empty();
    }

    public void excluirPorPedido(int pedidoId) {
        String sql = "DELETE FROM item_pedido WHERE pedido_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pedidoId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir itens do pedido", e);
        }
    }

    public List<ItemPedido> listarTodos() {
        String sql = "SELECT id, pedido_id, produto_id, quantidade, preco_unitario FROM item_pedido ORDER BY id";
        List<ItemPedido> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar todos os itens", e);
        }
        return lista;
    }

    private ItemPedido mapear(ResultSet rs) throws SQLException {
        ItemPedido item = new ItemPedido();
        item.setId(rs.getInt("id"));
        item.setPedidoId(rs.getInt("pedido_id"));
        item.setProdutoId(rs.getInt("produto_id"));
        item.setQuantidade(rs.getInt("quantidade"));
        item.setPrecoUnitario(rs.getDouble("preco_unitario"));
        return item;
    }
}