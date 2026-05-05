package br.delivery.service;

import br.delivery.model.ItemPedido;
import br.delivery.model.Pedido;

import java.util.List;

public class CalculadorPedido {

    private static final double TAXA_ENTREGA = 8.00;

    /**
     * Calcula e preenche todos os valores do pedido:
     * subtotal, desconto, taxaEntrega e valorFinal.
     */
    public void calcular(Pedido pedido, List<ItemPedido> itens) {
        double subtotal = calcularSubtotal(itens);
        double desconto = calcularDesconto(subtotal);
        double valorFinal = subtotal - desconto + TAXA_ENTREGA;

        pedido.setSubtotal(subtotal);
        pedido.setDesconto(desconto);
        pedido.setTaxaEntrega(TAXA_ENTREGA);
        pedido.setValorFinal(valorFinal);
    }

    public double calcularSubtotal(List<ItemPedido> itens) {
        return itens.stream()
                .mapToDouble(ItemPedido::getSubtotalItem)
                .sum();
    }

    public double calcularDesconto(double subtotal) {
        if (subtotal > 300.00) return subtotal * 0.15;
        if (subtotal > 200.00) return subtotal * 0.10;
        if (subtotal > 100.00) return subtotal * 0.05;
        return 0.00;
    }

    public double getPercentualDesconto(double subtotal) {
        if (subtotal > 300.00) return 15.0;
        if (subtotal > 200.00) return 10.0;
        if (subtotal > 100.00) return 5.0;
        return 0.0;
    }

    /**
     * Retorna um resumo detalhado do cálculo em texto.
     */
    public String gerarResumo(Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== RESUMO DO PEDIDO #").append(pedido.getId()).append(" ==========\n");
        sb.append(String.format("  Subtotal dos produtos : R$ %8.2f%n", pedido.getSubtotal()));
        if (pedido.getDesconto() > 0) {
            double pct = getPercentualDesconto(pedido.getSubtotal());
            sb.append(String.format("  Desconto (%.0f%%)        : R$ %8.2f%n", pct, pedido.getDesconto()));
        } else {
            sb.append(String.format("  Desconto              : R$ %8.2f%n", 0.0));
        }
        sb.append(String.format("  Taxa de entrega       : R$ %8.2f%n", pedido.getTaxaEntrega()));
        sb.append("  ----------------------------------------\n");
        sb.append(String.format("  VALOR FINAL           : R$ %8.2f%n", pedido.getValorFinal()));
        sb.append("=========================================\n");
        return sb.toString();
    }
}