

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Cliente;

public class ClienteDAO {

    private final DatabaseConnection db;
    private final List<Cliente> tabela = new ArrayList<>();
    private int proximoId = 1;

    public ClienteDAO(DatabaseConnection db) {
        this.db = db;
    }

    public void inserir(Cliente c) {
        c.setId(proximoId++);
        tabela.add(c);
        System.out.println("✔ Cliente '" + c.getNome() + "' cadastrado com ID " + c.getId());
    }

    public List<Cliente> listarTodos() {
        return new ArrayList<>(tabela);
    }

    public Optional<Cliente> buscarPorId(int id) {
        return tabela.stream().filter(c -> c.getId() == id).findFirst();
    }

    public boolean atualizar(Cliente atualizado) {
        for (int i = 0; i < tabela.size(); i++) {
            if (tabela.get(i).getId() == atualizado.getId()) {
                tabela.set(i, atualizado);
                return true;
            }
        }
        return false;
    }

    public boolean excluir(int id) {
        return tabela.removeIf(c -> c.getId() == id);
    }
}