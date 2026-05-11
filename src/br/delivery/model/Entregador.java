package br.delivery.model;

public class Entregador {
    private int id;
    private String nome;
    private String telefone;
    private String veiculo;
    private StatusEntregador status;

    public Entregador() {
        this.status = StatusEntregador.DISPONIVEL;
    }

    public Entregador(String nome, String telefone, String veiculo) {
        this.nome = nome;
        this.telefone = telefone;
        this.veiculo = veiculo;
        this.status = StatusEntregador.DISPONIVEL;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getVeiculo() { return veiculo; }
    public void setVeiculo(String veiculo) { this.veiculo = veiculo; }
    public StatusEntregador getStatus() { return status; }
    public void setStatus(StatusEntregador status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | Status: %s", id, nome, veiculo, telefone, status.getDescricao());
    }
}