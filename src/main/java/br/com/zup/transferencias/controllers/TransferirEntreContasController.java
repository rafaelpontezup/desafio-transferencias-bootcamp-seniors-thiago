package br.com.zup.transferencias.controllers;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.math.BigDecimal;
import java.net.URI;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.zup.transferencias.controllers.requests.TransferirEntreContasRequest;
import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Transferencia;
import br.com.zup.transferencias.repositories.ContaRepository;

@RestController
@RequestMapping("/transferencias")
public class TransferirEntreContasController {

    Logger logger = LoggerFactory.getLogger(TransferirEntreContasController.class);

    private final ContaRepository contaRepository;

    public TransferirEntreContasController(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional
    @PostMapping
    public ResponseEntity<?> transferir(@RequestBody @Valid TransferirEntreContasRequest request,
                                        UriComponentsBuilder uriBuilder) {
        Long origemId = request.getOrigemId();
        Long destinoId = request.getDestinoId();
        BigDecimal valor = request.getValor();

        if (origemId.equals(destinoId)) {
            logger.warn(
                "Não foi possível realizar a transferência, pois as contas de origem e de destino não podem ser iguais (id = {}).",
                origemId
            );

            throw new ResponseStatusException(
                UNPROCESSABLE_ENTITY, "As contas de origem e de destino não podem ser iguais."
            );
        }

        Conta origem = contaRepository.findById(origemId)
                                      .orElseThrow(
                                          () -> new ResponseStatusException(
                                              NOT_FOUND,
                                              "Não existe uma conta de origem com o id informado."
                                          )
                                      );

        Conta destino = contaRepository.findById(destinoId)
                                       .orElseThrow(
                                           () -> new ResponseStatusException(
                                               NOT_FOUND,
                                               "Não existe uma conta de destino com o id informado."
                                           )
                                       );

        if (!origem.possuiSaldoSuficienteParaTransferir(valor)) {
            logger.warn(
                "Não foi possível realizar a transferência de valor {} para a {}, pois a {} não possui saldo suficiente.",
                valor, destino, origem
            );

            throw new ResponseStatusException(
                UNPROCESSABLE_ENTITY, "A conta de origem não possui saldo suficiente."
            );
        }

        Transferencia transferencia = request.toModel(origem, destino);
        origem.enviar(transferencia);
        destino.receber(transferencia);

        contaRepository.flush();
        logger.info("{} realizada com sucesso.", transferencia);

        URI location = uriBuilder.path("/transferencias/{id}")
                                 .buildAndExpand(transferencia.getId())
                                 .toUri();

        return ResponseEntity.created(location).build();
    }

}
