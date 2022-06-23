package br.com.zup.transferencias.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transferencias")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Conta origem;

    @ManyToOne(optional = false)
    private Conta destino;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    public Transferencia() {}

    public Transferencia(Conta origem, Conta destino, BigDecimal valor) {
        this.origem = origem;
        this.destino = destino;
        this.valor = valor;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Conta getOrigem() {
        return origem;
    }

    public Conta getDestino() {
        return destino;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return "Transferencia [id=" + id + ", valor=" + valor + ", origem=" + origem + ", destino="
                + destino + ", dataHora=" + dataHora + "]";
    }

}
