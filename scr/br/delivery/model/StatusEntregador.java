

public enum StatusEntregador {
    DISPONIVEL("Disponível"),
    EM_ENTREGA("Em entrega"),
    INATIVO("Inativo");

    private final String descricao;

    StatusEntregador(String descricao) {
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