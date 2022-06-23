package br.com.zup.transferencias.controllers;

import static br.com.zup.transferencias.controllers.responses.TipoTransferenciaResponse.ENVIADA;
import static br.com.zup.transferencias.controllers.responses.TipoTransferenciaResponse.RECEBIDA;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.zup.transferencias.controllers.exceptions.ErroPadrao;
import br.com.zup.transferencias.controllers.responses.ListarTransferenciasResponse;
import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Cpf;
import br.com.zup.transferencias.models.Transferencia;
import br.com.zup.transferencias.repositories.ContaRepository;
import br.com.zup.transferencias.repositories.TransferenciaRepository;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
public class ListarTransferenciasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransferenciaRepository transferenciaRepository;

    private Conta conta1;
    private Conta conta2;
    private Conta conta3;

    private Transferencia transferencia1;
    private Transferencia transferencia2;
    private Transferencia transferencia3;
    private Transferencia transferencia4;

    @BeforeEach
    void setUp() {
        transferenciaRepository.deleteAll();
        contaRepository.deleteAll();

        conta1 = new Conta("0001", "123456", "jose@example.com", new Cpf("123.456.789-09"), "José");
        conta2 = new Conta("0001", "654321", "joao@example.com", new Cpf("987.654.321-00"), "João");
        conta3 = new Conta("0001", "223344", "anna@example.com", new Cpf("111.111.110-30"), "Anna");

        conta1.setSaldo(new BigDecimal("500.00"));
        conta2.setSaldo(new BigDecimal("500.00"));
        conta3.setSaldo(new BigDecimal("500.00"));

        contaRepository.save(conta1);
        contaRepository.save(conta2);
        contaRepository.save(conta3);

        transferencia1 = new Transferencia(conta1, conta2, new BigDecimal("20.00"));
        transferencia2 = new Transferencia(conta1, conta2, new BigDecimal("30.00"));
        transferencia3 = new Transferencia(conta2, conta1, new BigDecimal("10.00"));
        transferencia4 = new Transferencia(conta2, conta1, new BigDecimal("50.00"));

        conta1.enviar(transferencia1);
        conta1.enviar(transferencia2);
        conta1.receber(transferencia3);
        conta1.receber(transferencia4);

        conta2.receber(transferencia1);
        conta2.receber(transferencia2);
        conta2.enviar(transferencia3);
        conta2.enviar(transferencia4);

        contaRepository.save(conta1);
        contaRepository.save(conta2);
        contaRepository.save(conta3);
    }

    @AfterEach
    void afterEach() {
        transferenciaRepository.deleteAll();
        contaRepository.deleteAll();
    }

    @Test
    void deveListarAsTransferenciasDeUmaContaNoTamanhoEscolhido() throws Exception {
        // cenario (given)
        MockHttpServletRequestBuilder requestBuilder = get(
            "/contas/{id}/transferencias?size={size}", conta1.getId(), 4
        ).contentType(APPLICATION_JSON);

        // acao (when) e corretude (then)
        String response = mockMvc.perform(requestBuilder)
                                 .andExpect(status().isOk())
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString(UTF_8);

        Page<ListarTransferenciasResponse> page = objectMapper.readValue(
            response, new TypeReference<TestPageImpl<ListarTransferenciasResponse>>() {}
        );
        List<ListarTransferenciasResponse> transferencias = page.getContent();

        assertThat(transferencias).hasSize(4)
                                  .extracting("valor", "tipo", "titular", "agencia", "numero")
                                  .contains(
                                      new Tuple(
                                          transferencia1.getValor(), ENVIADA, conta2.getTitular(),
                                          conta2.getAgencia(), conta2.getNumero()
                                      ),
                                      new Tuple(
                                          transferencia2.getValor(), ENVIADA, conta2.getTitular(),
                                          conta2.getAgencia(), conta2.getNumero()
                                      ),
                                      new Tuple(
                                          transferencia3.getValor(), RECEBIDA, conta2.getTitular(),
                                          conta2.getAgencia(), conta2.getNumero()
                                      ),
                                      new Tuple(
                                          transferencia4.getValor(), RECEBIDA, conta2.getTitular(),
                                          conta2.getAgencia(), conta2.getNumero()
                                      )
                                  );
    }

    @Test
    void deveListarAsTransferenciasDeUmaContaNaOrdemEscolhida() throws Exception {
        // cenario (given)
        MockHttpServletRequestBuilder requestBuilder = get(
            "/contas/{id}/transferencias?sort=valor,desc", conta1.getId()
        ).contentType(APPLICATION_JSON);

        // acao (when) e corretude (then)
        String response = mockMvc.perform(requestBuilder)
                                 .andExpect(status().isOk())
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString(UTF_8);

        Page<ListarTransferenciasResponse> page = objectMapper.readValue(
            response, new TypeReference<TestPageImpl<ListarTransferenciasResponse>>() {}
        );
        List<ListarTransferenciasResponse> transferencias = page.getContent();

        assertThat(transferencias).hasSize(2)
                                  .extracting("valor", "tipo", "titular", "agencia", "numero")
                                  .containsExactly(
                                      new Tuple(
                                          transferencia4.getValor(), RECEBIDA, conta2.getTitular(),
                                          conta2.getAgencia(), conta2.getNumero()
                                      ),
                                      new Tuple(
                                          transferencia2.getValor(), ENVIADA, conta2.getTitular(),
                                          conta2.getAgencia(), conta2.getNumero()
                                      )
                                  );
    }

    @Test
    void deveListarAsTransferenciasDeUmaContaNaPaginaEscolhida() throws Exception {
        // cenario (given)
        MockHttpServletRequestBuilder requestBuilder = get(
            "/contas/{id}/transferencias?sort=valor,desc&page=1", conta1.getId()
        ).contentType(APPLICATION_JSON);

        // acao (when) e corretude (then)
        String response = mockMvc.perform(requestBuilder)
                                 .andExpect(status().isOk())
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString(UTF_8);

        Page<ListarTransferenciasResponse> page = objectMapper.readValue(
            response, new TypeReference<TestPageImpl<ListarTransferenciasResponse>>() {}
        );
        List<ListarTransferenciasResponse> transferencias = page.getContent();

        assertThat(transferencias).hasSize(2)
                                  .extracting("valor", "tipo", "titular", "agencia", "numero")
                                  .containsExactly(
                                      new Tuple(
                                          transferencia1.getValor(), ENVIADA, conta2.getTitular(),
                                          conta2.getAgencia(), conta2.getNumero()
                                      ),
                                      new Tuple(
                                          transferencia3.getValor(), RECEBIDA, conta2.getTitular(),
                                          conta2.getAgencia(), conta2.getNumero()
                                      )
                                  );
    }

    @Test
    void deveRetornarUmaColecaoVaziaParaUmaContaSemTransferencias() throws Exception {
        // cenario (given)
        MockHttpServletRequestBuilder requestBuilder = get(
            "/contas/{id}/transferencias", conta3.getId()
        ).contentType(APPLICATION_JSON);

        // acao (when) e corretude (then)
        String response = mockMvc.perform(requestBuilder)
                                 .andExpect(status().isOk())
                                 .andReturn()
                                 .getResponse()
                                 .getContentAsString(UTF_8);

        Page<ListarTransferenciasResponse> page = objectMapper.readValue(
            response, new TypeReference<TestPageImpl<ListarTransferenciasResponse>>() {}
        );
        List<ListarTransferenciasResponse> transferencias = page.getContent();

        assertThat(transferencias).isEmpty();
    }

    @Test
    void naoDeveListarAsTransferenciasDeUmaContaNaoCadastrada() throws Exception {
        // cenario (given)
        MockHttpServletRequestBuilder requestBuilder = get(
            "/contas/{id}/transferencias", Long.MAX_VALUE
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
