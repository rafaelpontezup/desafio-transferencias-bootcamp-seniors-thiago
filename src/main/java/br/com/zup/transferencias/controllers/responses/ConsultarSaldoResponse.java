package br.com.zup.transferencias.controllers.responses;

import java.math.BigDecimal;

import br.com.zup.transferencias.models.Conta;

public class ConsultarSaldoResponse {

    private String agencia;
    private String numero;
    private BigDecimal saldo;

    public ConsultarSaldoResponse() {}

    public ConsultarSaldoResponse(Conta conta) {
        this.agencia = conta.getAgencia();
        this.numero = conta.getNumero();
        this.saldo = conta.getSaldo();
    }

    public String getAgencia() {
        return agencia;
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

}
