package br.com.zup.transferencias.controllers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.zup.transferencias.controllers.exceptions.ErroPadrao;
import br.com.zup.transferencias.controllers.requests.CadastrarContaRequest;
import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Cpf;
import br.com.zup.transferencias.repositories.ContaRepository;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
public class CadastrarContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContaRepository contaRepository;

    @BeforeEach
    void setUp() {
        contaRepository.deleteAll();
    }

    @AfterEach
    void afterEach() {
        contaRepository.deleteAll();
    }

    @Test
    void deveCadastrarUmaConta() throws Exception {
        // cenario (given)
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        CadastrarContaRequest request = new CadastrarContaRequest(
            "0001", "123456", "thiago@example.com", "123.456.789-09", "Thiago"
        );

        String payload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/contas").contentType(APPLICATION_JSON)
                                                                      .content(payload);

        // acao (when) e corretude (then)
        mockMvc.perform(requestBuilder)
               .andExpect(status().isCreated())
               .andExpect(redirectedUrlPattern(baseUrl + "/contas/*"));

        List<Conta> contas = contaRepository.findAll();

        assertEquals(1, contas.size());
    }

    @Test
    void naoDeveCadastrarUmaContaComDadosNulos() throws Exception {
        // cenario (given)
        CadastrarContaRequest request = new CadastrarContaRequest(null, null, null, null, null);

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/contas").contentType(APPLICATION_JSON)
                                                                      .content(requestPayload)
                                                                      .header(
                                                                          "Accept-Language", "pt-br"
                                                                      );

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isBadRequest())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(5)
                             .contains(
                                 "agencia: n??o deve estar em branco",
                                 "numero: n??o deve estar em branco",
                                 "email: n??o deve estar em branco", "cpf: n??o deve estar em branco",
                                 "titular: n??o deve estar em branco"
                             );
    }

    @Test
    // Teste das validacoes de email, cpf e titular
    void naoDeveCadastrarUmaContaComDadosInvalidos() throws Exception {
        // cenario (given)
        CadastrarContaRequest request = new CadastrarContaRequest(
            "0001", "123456", "thiago@example,com", "123.456.789-00",
            "Pedro de Alc??ntara Francisco Ant??nio Jo??o Carlos Xavier de Paula Miguel Rafael "
                    + "Joaquim Jos?? Gonzaga Pascoal Cipriano Serafim de Bragan??a e Bourbon"
        );

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/contas").contentType(APPLICATION_JSON)
                                                                      .content(requestPayload)
                                                                      .header(
                                                                          "Accept-Language", "pt-br"
                                                                      );

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isBadRequest())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(
            mensagens
        ).hasSize(3)
         .contains(
             "email: deve ser um endere??o de e-mail bem formado",
             "cpf: n??mero do registro de contribuinte individual brasileiro (CPF) inv??lido",
             "titular: tamanho deve ser entre 0 e 120"
         );
    }

    @Test
    // Teste das validacoes de pattern de agencia, numero e cpf
    void naoDeveCadastrarUmaContaComDadosComPatternsInvalidos() throws Exception {
        // cenario (given)
        CadastrarContaRequest request = new CadastrarContaRequest(
            "001", "123A45", "thiago@example.com", "12345678909", "Thiago"
        );

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/contas").contentType(APPLICATION_JSON)
                                                                      .content(requestPayload);

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isBadRequest())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(3)
                             .contains(
                                 "agencia: deve ser composta por exatamente 4 d??gitos num??ricos",
                                 "numero: deve ser composto por exatamente 6 d??gitos num??ricos",
                                 "cpf: deve estar no formato XXX.XXX.XXX-XX"
                             );
    }

    @Test
    void naoDeveCadastrarUmaContaComEmailRepetido() throws Exception {
        // cenario (given)
        contaRepository.save(
            new Conta("0001", "123456", "thiago@example.com", new Cpf("123.456.789-09"), "Thiago")
        );

        CadastrarContaRequest request = new CadastrarContaRequest(
            "0001", "123457", "thiago@example.com", "987.654.321-00", "Thiago"
        );

        String payload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/contas").contentType(APPLICATION_JSON)
                                                                      .content(payload);

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isUnprocessableEntity())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(1)
                             .contains("J?? existe uma conta cadastrada com o email informado.");
    }

    @Test
    void naoDeveCadastrarUmaContaComCpfRepetido() throws Exception {
        // cenario (given)
        contaRepository.save(
            new Conta("0001", "123456", "thiago@example.com", new Cpf("123.456.789-09"), "Thiago")
        );

        CadastrarContaRequest request = new CadastrarContaRequest(
            "0001", "123457", "thiagocavalcante@example.com", "123.456.789-09", "Thiago"
        );

        String payload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/contas").contentType(APPLICATION_JSON)
                                                                      .content(payload);

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isUnprocessableEntity())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(1)
                             .contains("J?? existe uma conta cadastrada com o CPF informado.");
    }

}
