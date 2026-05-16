package br.delivery.dao;

import br.delivery.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProdutoDAO {

    private final DatabaseConnection db;

    public ProdutoDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Produto p) {
        String sql = "INSERT INTO produto (restaurante_id, nome, descricao, preco) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getRestauranteId());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getDescricao());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(p.getPreco()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
            System.out.println("✔ Produto '" + p.getNome() + "' cadastrado com ID " + p.getId());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto", e);
        }
    }

    public List<Produto> listarTodos() {
        String sql = "SELECT id, restaurante_id, nome, descricao, preco FROM produto ORDER BY id";
        List<Produto> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos", e);
        }
        return lista;
    }

    public List<Produto> listarPorRestaurante(int restauranteId) {
        String sql = "SELECT id, restaurante_id, nome, descricao, preco FROM produto WHERE restaurante_id = ? ORDER BY id";
        List<Produto> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, restauranteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos por restaurante", e);
        }
        return lista;
    }

    public Optional<Produto> buscarPorId(int id) {
        String sql = "SELECT id, restaurante_id, nome, descricao, preco FROM produto WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto", e);
        }
        return Optional.empty();
    }

    public boolean atualizar(Produto p) {
        String sql = "UPDATE produto SET restaurante_id=?, nome=?, descricao=?, preco=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getRestauranteId());
            ps.setString(2, p.getNome());
            ps.setString(3, p.getDescricao());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(p.getPreco()));
            ps.setInt(5, p.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto", e);
        }
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM produto WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir produto", e);
        }
    }

    private Produto mapear(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setRestauranteId(rs.getInt("restaurante_id"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        p.setPreco(rs.getDouble("preco"));
        return p;
    }
}