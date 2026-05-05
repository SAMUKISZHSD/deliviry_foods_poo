

public enum StatusPedido {
    AGUARDANDO("Aguardando confirmação"),
    CONFIRMADO("Confirmado"),
    EM_PREPARO("Em preparo"),
    SAIU_ENTREGA("Saiu para entrega"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}