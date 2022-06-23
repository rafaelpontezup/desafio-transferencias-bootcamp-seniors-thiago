package br.com.zup.transferencias.controllers.requests;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Transferencia;

public class TransferirEntreContasRequest {

    @NotNull
    @Positive
    private Long origemId;

    @NotNull
    @Positive
    private Long destinoId;

    @NotNull
    @Positive
    private BigDecimal valor;

    public TransferirEntreContasRequest() {}

    public TransferirEntreContasRequest(Long origemId, Long destinoId, BigDecimal valor) {
        this.origemId = origemId;
        this.destinoId = destinoId;
        this.valor = valor;
    }

    public Transferencia toModel(Conta origem, Conta destino) {
        return new Transferencia(origem, destino, valor);
    }

    public Long getOrigemId() {
        return origemId;
    }

    public Long getDestinoId() {
        return destinoId;
    }

    public BigDecimal getValor() {
        return valor;
    }

}
