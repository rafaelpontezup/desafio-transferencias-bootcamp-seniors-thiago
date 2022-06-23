package br.com.zup.transferencias.controllers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.zup.transferencias.controllers.exceptions.ErroPadrao;
import br.com.zup.transferencias.controllers.responses.ConsultarSaldoResponse;
import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Cpf;
import br.com.zup.transferencias.repositories.ContaRepository;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
public class ConsultarSaldoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContaRepository contaRepository;

    private Conta conta;

    @BeforeEach
    void setUp() {
        contaRepository.deleteAll();

        conta = new Conta(
            "0001", "123456", "thiago@example.com", new Cpf("123.456.789-09"), "Thiago"
        );
        conta.setSaldo(new BigDecimal("500.00"));

        contaRepository.save(conta);
    }

    @AfterEach
    void afterEach() {
        contaRepository.deleteAll();
    }

    @Test
    void deveConsultarSaldoDeUmaConta() throws Exception {
        // cenario (given)
        MockHttpServletRequestBuilder requestBuilder = get(
            "/contas/{id}", conta.getId()
        ).contentType(APPLICATION_JSON);

        // acao (when) e corretude (then)
        String response = mockMvc.perform(requestBuilder)
                                 .andExpect(status().isOk())
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString(UTF_8);

        ConsultarSaldoResponse consultarSaldoResponse = objectMapper.readValue(
            response, ConsultarSaldoResponse.class
        );

        assertThat(consultarSaldoResponse).extracting("agencia", "numero", "saldo")
                                          .contains(
                                              conta.getAgencia(), conta.getNumero(),
                                              conta.getSaldo()
                                          );
    }

    @Test
    void naoDeveConsultarSaldoDeUmaContaNaoCadastrada() throws Exception {
        // cenario (given)
        MockHttpServletRequestBuilder requestBuilder = get(
            "/contas/{id}", Long.MAX_VALUE
        ).contentType(APPLICATION_JSON);

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isNotFound())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(1).contains("Não existe uma conta com o id informado.");
    }

}
