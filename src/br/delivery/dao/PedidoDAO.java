package br.delivery.dao;

import br.delivery.model.Pedido;
import br.delivery.model.StatusPedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PedidoDAO {

    private final DatabaseConnection db;

    public PedidoDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Pedido p) {
        String sql = """
            INSERT INTO pedido
                (cliente_id, restaurante_id, entregador_id, status,
                 subtotal, desconto, taxa_entrega, valor_final, data_criacao)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getClienteId());
            ps.setInt(2, p.getRestauranteId());
            if (p.getEntregadorId() != null) ps.setInt(3, p.getEntregadorId());
            else                              ps.setNull(3, Types.INTEGER);
            ps.setString(4, p.getStatus().name());
            ps.setBigDecimal(5, bd(p.getSubtotal()));
            ps.setBigDecimal(6, bd(p.getDesconto()));
            ps.setBigDecimal(7, bd(p.getTaxaEntrega()));
            ps.setBigDecimal(8, bd(p.getValorFinal()));
            ps.setTimestamp(9, Timestamp.valueOf(p.getDataCriacao()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
            System.out.println("✔ Pedido #" + p.getId() + " criado com sucesso.");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir pedido", e);
        }
    }

    public List<Pedido> listarTodos() {
        return buscarLista("SELECT * FROM pedido ORDER BY id", ps -> {});
    }

    public List<Pedido> listarPorCliente(int clienteId) {
        return buscarLista("SELECT * FROM pedido WHERE cliente_id = ? ORDER BY id",
                ps -> ps.setInt(1, clienteId));
    }

    public List<Pedido> listarPorRestaurante(int restauranteId) {
        return buscarLista("SELECT * FROM pedido WHERE restaurante_id = ? ORDER BY id",
                ps -> ps.setInt(1, restauranteId));
    }

    public Optional<Pedido> buscarPorId(int id) {
        String sql = "SELECT * FROM pedido WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedido", e);
        }
        return Optional.empty();
    }

    public boolean atualizarStatus(int pedidoId, StatusPedido novoStatus) {
        String sql = "UPDATE pedido SET status = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, novoStatus.name());
            ps.setInt(2, pedidoId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do pedido", e);
        }
    }

    public boolean atualizar(Pedido p) {
        String sql = """
            UPDATE pedido
            SET cliente_id=?, restaurante_id=?, entregador_id=?, status=?,
                subtotal=?, desconto=?, taxa_entrega=?, valor_final=?
            WHERE id=?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getClienteId());
            ps.setInt(2, p.getRestauranteId());
            if (p.getEntregadorId() != null) ps.setInt(3, p.getEntregadorId());
            else                              ps.setNull(3, Types.INTEGER);
            ps.setString(4, p.getStatus().name());
            ps.setBigDecimal(5, bd(p.getSubtotal()));
            ps.setBigDecimal(6, bd(p.getDesconto()));
            ps.setBigDecimal(7, bd(p.getTaxaEntrega()));
            ps.setBigDecimal(8, bd(p.getValorFinal()));
            ps.setInt(9, p.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar pedido", e);
        }
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM pedido WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir pedido", e);
        }
    }

    // ---------------------------------------------------------------
    // Helpers internos
    // ---------------------------------------------------------------
    @FunctionalInterface
    private interface PsSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<Pedido> buscarLista(String sql, PsSetter setter) {
        List<Pedido> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos", e);
        }
        return lista;
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setClienteId(rs.getInt("cliente_id"));
        p.setRestauranteId(rs.getInt("restaurante_id"));

        int entregadorId = rs.getInt("entregador_id");
        p.setEntregadorId(rs.wasNull() ? null : entregadorId);

        p.setStatus(StatusPedido.valueOf(rs.getString("status")));
        p.setSubtotal(rs.getDouble("subtotal"));
        p.setDesconto(rs.getDouble("desconto"));
        p.setTaxaEntrega(rs.getDouble("taxa_entrega"));
        p.setValorFinal(rs.getDouble("valor_final"));

        Timestamp ts = rs.getTimestamp("data_criacao");
        if (ts != null) p.setDataCriacao(ts.toLocalDateTime());

        return p;
    }

    private java.math.BigDecimal bd(double valor) {
        return java.math.BigDecimal.valueOf(valor);
    }
}