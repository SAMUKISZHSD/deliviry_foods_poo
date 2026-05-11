package br.delivery.model;

public class Restaurante {
    private int id;
    private String nome;
    private String endereco;
    private String telefone;
    private String categoria;

    public Restaurante() {}

    public Restaurante(String nome, String endereco, String telefone, String categoria) {
        this.nome = nome;
        this.endereco = endereco;
        this.telefone = telefone;
        this.categoria = categoria;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | Tel: %s", id, nome, categoria, endereco, telefone);
    }
}