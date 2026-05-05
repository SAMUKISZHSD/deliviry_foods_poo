

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import Entregador;
import StatusEntregador;

public class EntregadorDAO {

    private final DatabaseConnection db;
    private final List<Entregador> tabela = new ArrayList<>();
    private int proximoId = 1;

    public EntregadorDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Entregador e) {
        e.setId(proximoId++);
        tabela.add(e);
        System.out.println("✔ Entregador '" + e.getNome() + "' cadastrado com ID " + e.getId());
    }

    public List<Entregador> listarTodos() {
        return new ArrayList<>(tabela);
    }

    public List<Entregador> listarDisponiveis() {
        return tabela.stream()
                .filter(e -> e.getStatus() == StatusEntregador.DISPONIVEL)
                .collect(Collectors.toList());
    }

    public Optional<Entregador> buscarPorId(int id) {
        return tabela.stream().filter(e -> e.getId() == id).findFirst();
    }

    public boolean atualizar(Entregador atualizado) {
        for (int i = 0; i < tabela.size(); i++) {
            if (tabela.get(i).getId() == atualizado.getId()) {
                tabela.set(i, atualizado);
                return true;
            }
        }
        return false;
    }

    public boolean excluir(int id) {
        return tabela.removeIf(e -> e.getId() == id);
    }
}