package br.com.zup.transferencias.models;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

@Entity
@Table(name = "contas", uniqueConstraints = {
        @UniqueConstraint(name = "UK_CONTA_EMAIL", columnNames = "email"),
        @UniqueConstraint(name = "UK_CONTA_HASH_CPF", columnNames = "hash_cpf")})
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String agencia;

    @Column(nullable = false)
    private String numero;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "numero", column = @Column(name = "numero_cpf", nullable = false, length = 14)),
            @AttributeOverride(name = "hash", column = @Column(name = "hash_cpf", nullable = false, length = 32))})
    private Cpf cpf;

    @Column(nullable = false)
    private String titular;

    @Column(nullable = false)
    private BigDecimal saldo;

    @OneToMany(mappedBy = "origem", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Transferencia> transferenciasEnviadas;

    @OneToMany(mappedBy = "destino")
    private Set<Transferencia> transferenciasRecebidas;

    @Version
    private int versao;

    /**
     * @deprecated Construtor de uso exclusivo do Hibernate
     */
    @Deprecated
    public Conta() {}

    public Conta(String agencia, String numero, String email, Cpf cpf, String titular) {
        this.agencia = agencia;
        this.numero = numero;
        this.email = email;
        this.cpf = cpf;
        this.titular = titular;
        this.saldo = BigDecimal.ZERO;
        this.transferenciasEnviadas = new HashSet<>();
        this.transferenciasRecebidas = new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public String getAgencia() {
        return agencia;
    }

    public String getNumero() {
        return numero;
    }

    public String getTitular() {
        return titular;
    }

    public Set<Transferencia> getTransferencias() {
        Set<Transferencia> transferencias = new HashSet<>();
        transferencias.addAll(transferenciasEnviadas);
        transferencias.addAll(transferenciasRecebidas);

        return transferencias;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    public boolean possuiSaldoSuficienteParaTransferir(BigDecimal valor) {
        return this.saldo.compareTo(valor) >= 0;
    }

    public void enviar(Transferencia transferencia) {
        this.transferenciasEnviadas.add(transferencia);
        this.setSaldo(this.saldo.subtract(transferencia.getValor()));
    }

    public void receber(Transferencia transferencia) {
        this.transferenciasRecebidas.add(transferencia);
        this.setSaldo(this.saldo.add(transferencia.getValor()));
    }

    @Override
    public String toString() {
        return "Conta [id=" + id + ", agencia=" + agencia + ", numero=" + numero + ", titular="
                + titular + ", cpf=" + cpf + ", email=" + email + "]";
    }

}
