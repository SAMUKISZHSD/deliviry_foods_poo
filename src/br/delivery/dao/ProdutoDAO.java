package br.delivery.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import br.delivery.model.*;

public class ProdutoDAO {

    private final DatabaseConnection db;
    private final List<Produto> tabela = new ArrayList<>();
    private int proximoId = 1;

    public ProdutoDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Produto p) {
        p.setId(proximoId++);
        tabela.add(p);
        System.out.println("✔ Produto '" + p.getNome() + "' cadastrado com ID " + p.getId());
    }

    public List<Produto> listarTodos() {
        return new ArrayList<>(tabela);
    }

    public List<Produto> listarPorRestaurante(int restauranteId) {
        return tabela.stream()
                .filter(p -> p.getRestauranteId() == restauranteId)
                .collect(Collectors.toList());
    }

    public Optional<Produto> buscarPorId(int id) {
        return tabela.stream().filter(p -> p.getId() == id).findFirst();
    }

    public boolean atualizar(Produto atualizado) {
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