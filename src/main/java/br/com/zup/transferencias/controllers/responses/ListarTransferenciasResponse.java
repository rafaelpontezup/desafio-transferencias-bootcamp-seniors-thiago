package br.com.zup.transferencias.controllers.responses;

import static br.com.zup.transferencias.controllers.responses.TipoTransferenciaResponse.ENVIADA;
import static br.com.zup.transferencias.controllers.responses.TipoTransferenciaResponse.RECEBIDA;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Transferencia;

public class ListarTransferenciasResponse {

    private Long id;
    private BigDecimal valor;

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dataHora;

    private TipoTransferenciaResponse tipo;
    private String titular;
    private String agencia;
    private String numero;

    public ListarTransferenciasResponse() {}

    public ListarTransferenciasResponse(Conta conta, Transferencia transferencia) {
        boolean contaOrigem = conta.equals(transferencia.getOrigem());
        Conta outraConta = contaOrigem ? transferencia.getDestino() : transferencia.getOrigem();
        TipoTransferenciaResponse tipo = contaOrigem ? ENVIADA : RECEBIDA;

        this.id = transferencia.getId();
        this.valor = transferencia.getValor();
        this.dataHora = transferencia.getDataHora();
        this.tipo = tipo;
        this.titular = outraConta.getTitular();
        this.agencia = outraConta.getAgencia();
        this.numero = outraConta.getNumero();
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public TipoTransferenciaResponse getTipo() {
        return tipo;
    }

    public String getTitular() {
        return titular;
    }

    public String getAgencia() {
        return agencia;
    }

    public String getNumero() {
        return numero;
    }

}
