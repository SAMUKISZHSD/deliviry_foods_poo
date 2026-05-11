package br.delivery.model;

public class Produto {
    private int id;
    private int restauranteId;
    private String nome;
    private String descricao;
    private double preco;

    public Produto() {}

    public Produto(int restauranteId, String nome, String descricao, double preco) {
        this.restauranteId = restauranteId;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRestauranteId() { return restauranteId; }
    public void setRestauranteId(int restauranteId) { this.restauranteId = restauranteId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s | R$ %.2f", id, nome, descricao, preco);
    }
}