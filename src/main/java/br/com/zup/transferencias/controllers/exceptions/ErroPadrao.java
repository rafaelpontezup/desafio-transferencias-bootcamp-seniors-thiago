package br.com.zup.transferencias.controllers.exceptions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.FieldError;

public class ErroPadrao {

    private Integer codigoHttp;
    private String mensagemHttp;
    private List<String> mensagens;
    private String caminho;
    private LocalDateTime dataHora;

    public ErroPadrao() {}

    public ErroPadrao(Integer codigoHttp, String mensagemHttp, String caminho) {
        this.codigoHttp = codigoHttp;
        this.mensagemHttp = mensagemHttp;
        this.mensagens = new ArrayList<>();
        this.caminho = caminho;
        this.dataHora = LocalDateTime.now();
    }

    public void adicionar(String mensagem) {
        this.mensagens.add(mensagem);
    }

    public void adicionar(FieldError fieldError) {
        this.mensagens.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
    }

    public Integer getCodigoHttp() {
        return codigoHttp;
    }

    public String getMensagemHttp() {
        return mensagemHttp;
    }

    public List<String> getMensagens() {
        return mensagens;
    }

    public String getCaminho() {
        return caminho;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

}
