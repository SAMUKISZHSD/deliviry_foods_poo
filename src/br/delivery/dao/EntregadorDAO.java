package br.delivery.dao;

import br.delivery.model.Entregador;
import br.delivery.model.StatusEntregador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntregadorDAO {

    private final DatabaseConnection db;

    public EntregadorDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Entregador e) {
        String sql = "INSERT INTO entregador (nome, telefone, veiculo, status) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getNome());
            ps.setString(2, e.getTelefone());
            ps.setString(3, e.getVeiculo());
            ps.setString(4, e.getStatus().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) e.setId(rs.getInt(1));
            }
            System.out.println("✔ Entregador '" + e.getNome() + "' cadastrado com ID " + e.getId());

        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao inserir entregador", ex);
        }
    }

    public List<Entregador> listarTodos() {
        String sql = "SELECT id, nome, telefone, veiculo, status FROM entregador ORDER BY id";
        List<Entregador> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar entregadores", e);
        }
        return lista;
    }

    public List<Entregador> listarDisponiveis() {
        String sql = "SELECT id, nome, telefone, veiculo, status FROM entregador WHERE status = 'DISPONIVEL' ORDER BY id";
        List<Entregador> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar entregadores disponíveis", e);
        }
        return lista;
    }

    public Optional<Entregador> buscarPorId(int id) {
        String sql = "SELECT id, nome, telefone, veiculo, status FROM entregador WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar entregador", e);
        }
        return Optional.empty();
    }

    public boolean atualizar(Entregador e) {
        String sql = "UPDATE entregador SET nome=?, telefone=?, veiculo=?, status=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getNome());
            ps.setString(2, e.getTelefone());
            ps.setString(3, e.getVeiculo());
            ps.setString(4, e.getStatus().name());
            ps.setInt(5, e.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao atualizar entregador", ex);
        }
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM entregador WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir entregador", e);
        }
    }

    private Entregador mapear(ResultSet rs) throws SQLException {
        Entregador e = new Entregador();
        e.setId(rs.getInt("id"));
        e.setNome(rs.getString("nome"));
        e.setTelefone(rs.getString("telefone"));
        e.setVeiculo(rs.getString("veiculo"));
        e.setStatus(StatusEntregador.valueOf(rs.getString("status")));
        return e;
    }
}