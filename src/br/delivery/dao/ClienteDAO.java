package br.delivery.dao;

import br.delivery.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClienteDAO {

    private final DatabaseConnection db;

    public ClienteDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Cliente c) {
        String sql = "INSERT INTO cliente (nome, email, telefone, endereco) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getEndereco());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
            System.out.println("✔ Cliente '" + c.getNome() + "' cadastrado com ID " + c.getId());

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir cliente", e);
        }
    }

    public List<Cliente> listarTodos() {
        String sql = "SELECT id, nome, email, telefone, endereco FROM cliente ORDER BY id";
        List<Cliente> lista = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes", e);
        }
        return lista;
    }

    public Optional<Cliente> buscarPorId(int id) {
        String sql = "SELECT id, nome, email, telefone, endereco FROM cliente WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente", e);
        }
        return Optional.empty();
    }

    public boolean atualizar(Cliente c) {
        String sql = "UPDATE cliente SET nome=?, email=?, telefone=?, endereco=? WHERE id=?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getEndereco());
            ps.setInt(5, c.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar cliente", e);
        }
    }

    public boolean excluir(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir cliente", e);
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setEmail(rs.getString("email"));
        c.setTelefone(rs.getString("telefone"));
        c.setEndereco(rs.getString("endereco"));
        return c;
    }
}