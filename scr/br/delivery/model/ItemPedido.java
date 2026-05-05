

public class ItemPedido {
    private int id;
    private int pedidoId;
    private int produtoId;
    private int quantidade;
    private double precoUnitario;

    public ItemPedido() {}

    public ItemPedido(int pedidoId, int produtoId, int quantidade, double precoUnitario) {
        this.pedidoId = pedidoId;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }
    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(double precoUnitario) { this.precoUnitario = precoUnitario; }

    public double getSubtotalItem() {
        return quantidade * precoUnitario;
    }

    @Override
    public String toString() {
        return String.format("Produto #%d | Qtd: %d | R$ %.2f/un | Subtotal: R$ %.2f",
            produtoId, quantidade, precoUnitario, getSubtotalItem());
    }
}