

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Pedido {
    private int id;
    private int clienteId;
    private int restauranteId;
    private Integer entregadorId; // pode ser null antes de atribuir
    private StatusPedido status;
    private double subtotal;
    private double desconto;
    private double taxaEntrega;
    private double valorFinal;
    private LocalDateTime dataCriacao;

    private static final double TAXA_ENTREGA = 8.00;

    public Pedido() {
        this.status = StatusPedido.AGUARDANDO;
        this.taxaEntrega = TAXA_ENTREGA;
        this.dataCriacao = LocalDateTime.now();
    }

    public Pedido(int clienteId, int restauranteId) {
        this();
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }
    public int getRestauranteId() { return restauranteId; }
    public void setRestauranteId(int restauranteId) { this.restauranteId = restauranteId; }
    public Integer getEntregadorId() { return entregadorId; }
    public void setEntregadorId(Integer entregadorId) { this.entregadorId = entregadorId; }
    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDesconto() { return desconto; }
    public void setDesconto(double desconto) { this.desconto = desconto; }
    public double getTaxaEntrega() { return taxaEntrega; }
    public void setTaxaEntrega(double taxaEntrega) { this.taxaEntrega = taxaEntrega; }
    public double getValorFinal() { return valorFinal; }
    public void setValorFinal(double valorFinal) { this.valorFinal = valorFinal; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String entregador = entregadorId != null ? "Entregador #" + entregadorId : "Não atribuído";
        return String.format(
            "[#%d] Cliente: %d | Restaurante: %d | %s | Status: %s | R$ %.2f | %s",
            id, clienteId, restauranteId, entregador, status.getDescricao(), valorFinal,
            dataCriacao != null ? dataCriacao.format(fmt) : "-"
        );
    }
}