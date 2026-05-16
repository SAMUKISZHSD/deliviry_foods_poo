package br.delivery.dao;

import br.delivery.model.Pedido;
import br.delivery.model.Restaurante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RestauranteDAO {

    private final DatabaseConnection db;

    public RestauranteDAO(DatabaseConnection db) {
        this.db = db;
    }

    // ---------------------------------------------------------------
    // INSERT
    // ---------------------------------------------------------------
    public void inserir(Restaurante r) {
        String sql = "INSERT INTO restaurante (nome, endereco, telefone, categoria) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getNome());
            ps.setString(2, r.getEndereco());
            ps.setString(3, r.getTelefone());
            ps.setString(4, r.getCategoria());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    r.setId(rs.getInt(1));
                }
            }
            System.out.println("✔ Restaurante '" + r.getNome() + "' cadastrado com ID " + r.getId());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir restaurante", e);
        }
    }

    // ---------------------------------------------------------------
    // SELECT ALL
    // ---------------------------------------------------------------
    public List<Restaurante> listarTodos() {
        String sql = "SELECT id, nome, endereco, telefone, categoria FROM restaurante ORDER BY id";
        List<Restaurante> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar restaurantes", e);
        }
        return lista;
    }

    // ---------------------------------------------------------------
    // SELECT BY ID
    // ---------------------------------------------------------------
    public Optional<Restaurante> buscarPorId(int id) {
        String sql = "SELECT id, nome, endereco, telefone, categoria FROM restaurante WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar restaurante", e);
        }
        return Optional.empty();
    }

    // ---------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------
    public boolean atualizar(Restaurante r) {
        String sql = "UPDATE restaurante SET nome=?, endereco=?, telefone=?, categoria=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, r.getNome());
            ps.setString(2, r.getEndereco());
            ps.setString(3, r.getTelefone());
            ps.setString(4, r.getCategoria());
            ps.setInt(5, r.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar restaurante", e);
        }
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------
    public boolean excluir(int id) {
        String sql = "DELETE FROM restaurante WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir restaurante", e);
        }
    }

    // ---------------------------------------------------------------
    // Relatório de vendas
    // ---------------------------------------------------------------
    public void relatorioVendas(PedidoDAO pedidoDAO) {
        System.out.println("\n======= RELATÓRIO DE VENDAS POR RESTAURANTE =======");
        for (Restaurante r : listarTodos()) {
            List<Pedido> pedidos = pedidoDAO.listarPorRestaurante(r.getId());
            double total = pedidos.stream().mapToDouble(Pedido::getValorFinal).sum();
            System.out.printf("  %-25s | Pedidos: %3d | Total: R$ %8.2f%n",
                    r.getNome(), pedidos.size(), total);
        }
        System.out.println("====================================================\n");
    }

    // ---------------------------------------------------------------
    // Mapeamento ResultSet → Restaurante
    // ---------------------------------------------------------------
    private Restaurante mapear(ResultSet rs) throws SQLException {
        Restaurante r = new Restaurante();
        r.setId(rs.getInt("id"));
        r.setNome(rs.getString("nome"));
        r.setEndereco(rs.getString("endereco"));
        r.setTelefone(rs.getString("telefone"));
        r.setCategoria(rs.getString("categoria"));
        return r;
    }
}