package br.com.zup.transferencias.controllers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.zup.transferencias.controllers.exceptions.ErroPadrao;
import br.com.zup.transferencias.controllers.requests.TransferirEntreContasRequest;
import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Cpf;
import br.com.zup.transferencias.models.Transferencia;
import br.com.zup.transferencias.repositories.ContaRepository;
import br.com.zup.transferencias.repositories.TransferenciaRepository;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
public class TransferirEntreContasControllerTest {

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

    @BeforeEach
    void setUp() {
        transferenciaRepository.deleteAll();
        contaRepository.deleteAll();

        conta1 = new Conta("0001", "123456", "jose@example.com", new Cpf("123.456.789-09"), "José");
        conta1.setSaldo(new BigDecimal("100.00"));

        conta2 = new Conta("0001", "654321", "joao@example.com", new Cpf("987.654.321-00"), "João");

        contaRepository.save(conta1);
        contaRepository.save(conta2);
    }

    @AfterEach
    void afterEach() {
        transferenciaRepository.deleteAll();
        contaRepository.deleteAll();
    }

    @Test
    void deveTransferirEntreContas() throws Exception {
        // cenario (given)
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        Long origemId = conta1.getId();
        Long destinoId = conta2.getId();

        TransferirEntreContasRequest request = new TransferirEntreContasRequest(
            origemId, destinoId, new BigDecimal("60.00")
        );

        String payload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/transferencias")
                                                                              .contentType(
                                                                                  APPLICATION_JSON
                                                                              )
                                                                              .content(payload);

        // acao (when) e corretude (then)
        mockMvc.perform(requestBuilder)
               .andExpect(status().isCreated())
               .andExpect(redirectedUrlPattern(baseUrl + "/transferencias/*"));

        List<Transferencia> transferencias = transferenciaRepository.findAll();
        Conta origem = contaRepository.findById(origemId).get();
        Conta destino = contaRepository.findById(destinoId).get();

        assertEquals(1, transferencias.size());

        assertEquals(1, transferenciaRepository.countByOrigemId(origemId));
        assertEquals(0, transferenciaRepository.countByDestinoId(origemId));

        assertEquals(0, transferenciaRepository.countByOrigemId(destinoId));
        assertEquals(1, transferenciaRepository.countByDestinoId(destinoId));

        assertEquals(new BigDecimal("40.00"), origem.getSaldo());
        assertEquals(new BigDecimal("60.00"), destino.getSaldo());
    }

    @Test
    void naoDeveTransferirEntreContasComDadosNulos() throws Exception {
        // cenario (given)
        TransferirEntreContasRequest request = new TransferirEntreContasRequest(null, null, null);

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/transferencias").contentType(
            APPLICATION_JSON
        ).content(requestPayload).header("Accept-Language", "pt-br");

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
                                 "origemId: não deve ser nulo", "destinoId: não deve ser nulo",
                                 "valor: não deve ser nulo"
                             );
    }

    @Test
    void naoDeveTransferirEntreContasComDadosInvalidos() throws Exception {
        // cenario (given)
        TransferirEntreContasRequest request = new TransferirEntreContasRequest(
            -1L, -2L, new BigDecimal("-50.00")
        );

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/transferencias").contentType(
            APPLICATION_JSON
        ).content(requestPayload).header("Accept-Language", "pt-br");

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
                                 "origemId: deve ser maior que 0",
                                 "destinoId: deve ser maior que 0", "valor: deve ser maior que 0"
                             );
    }

    @Test
    void naoDeveTransferirEntreContasIguais() throws Exception {
        // cenario (given)
        TransferirEntreContasRequest request = new TransferirEntreContasRequest(
            conta1.getId(), conta1.getId(), new BigDecimal("50.00")
        );

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/transferencias").contentType(
            APPLICATION_JSON
        ).content(requestPayload);

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isUnprocessableEntity())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(1)
                             .contains("As contas de origem e de destino não podem ser iguais.");
    }

    @Test
    void naoDeveTransferirDeUmaContaNaoCadastrada() throws Exception {
        // cenario (given)
        TransferirEntreContasRequest request = new TransferirEntreContasRequest(
            Long.MAX_VALUE, conta2.getId(), new BigDecimal("50.00")
        );

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/transferencias").contentType(
            APPLICATION_JSON
        ).content(requestPayload);

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isNotFound())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(1)
                             .contains("Não existe uma conta de origem com o id informado.");
    }

    @Test
    void naoDeveTransferirParaUmaContaNaoCadastrada() throws Exception {
        // cenario (given)
        TransferirEntreContasRequest request = new TransferirEntreContasRequest(
            conta1.getId(), Long.MAX_VALUE, new BigDecimal("50.00")
        );

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/transferencias").contentType(
            APPLICATION_JSON
        ).content(requestPayload);

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isNotFound())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(1)
                             .contains("Não existe uma conta de destino com o id informado.");
    }

    @Test
    void naoDeveTransferirDeUmaContaSemSaldoSuficiente() throws Exception {
        // cenario (given)
        TransferirEntreContasRequest request = new TransferirEntreContasRequest(
            conta1.getId(), conta2.getId(), new BigDecimal("150.00")
        );

        String requestPayload = objectMapper.writeValueAsString(request);

        MockHttpServletRequestBuilder requestBuilder = post("/transferencias").contentType(
            APPLICATION_JSON
        ).content(requestPayload);

        // acao (when) e corretude (then)
        String responsePayload = mockMvc.perform(requestBuilder)
                                        .andExpect(status().isUnprocessableEntity())
                                        .andReturn()
                                        .getResponse()
                                        .getContentAsString(UTF_8);

        ErroPadrao erroPadrao = objectMapper.readValue(responsePayload, ErroPadrao.class);
        List<String> mensagens = erroPadrao.getMensagens();

        assertThat(mensagens).hasSize(1).contains("A conta de origem não possui saldo suficiente.");
    }

}
