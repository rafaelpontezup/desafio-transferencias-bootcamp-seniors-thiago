package br.com.zup.transferencias.controllers.requests;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.br.CPF;

import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Cpf;

public class CadastrarContaRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{4}$", message = "deve ser composta por exatamente 4 dígitos numéricos")
    private String agencia;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "deve ser composto por exatamente 6 dígitos numéricos")
    private String numero;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @CPF
    @Pattern(regexp = "^[0-9]{3}\\.[0-9]{3}\\.[0-9]{3}\\-[0-9]{2}$", message = "deve estar no formato XXX.XXX.XXX-XX")
    private String cpf;

    @NotBlank
    @Size(max = 120)
    private String titular;

    public CadastrarContaRequest() {}

    public CadastrarContaRequest(String agencia, String numero, String email, String cpf,
                                 String titular) {
        this.agencia = agencia;
        this.numero = numero;
        this.email = email;
        this.cpf = cpf;
        this.titular = titular;
    }

    public Conta toModel() {
        return new Conta(agencia, numero, email, new Cpf(cpf), titular);
    }

    public String getAgencia() {
        return agencia;
    }

    public String getNumero() {
        return numero;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTitular() {
        return titular;
    }

}
