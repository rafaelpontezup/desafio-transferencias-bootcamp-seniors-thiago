package br.com.zup.transferencias.controllers;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.net.URI;

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

import br.com.zup.transferencias.controllers.requests.CadastrarContaRequest;
import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.models.Cpf;
import br.com.zup.transferencias.repositories.ContaRepository;

@RestController
@RequestMapping("/contas")
public class CadastrarContaController {

    Logger logger = LoggerFactory.getLogger(CadastrarContaController.class);

    private final ContaRepository contaRepository;

    public CadastrarContaController(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody @Valid CadastrarContaRequest request,
                                       UriComponentsBuilder uriBuilder) {
        Conta conta = request.toModel();

        if (contaRepository.existsByEmail(request.getEmail())) {
            logger.warn(
                "Não foi possível cadastrar a {}, pois já existe uma conta cadastrada com o email informado.",
                conta
            );

            throw new ResponseStatusException(
                UNPROCESSABLE_ENTITY, "Já existe uma conta cadastrada com o email informado."
            );
        }

        if (contaRepository.existsByCpf_Hash(new Cpf(request.getCpf()).getHash())) {
            logger.warn(
                "Não foi possível cadastrar a {}, pois já existe uma conta cadastrada com o CPF informado.",
                conta
            );

            throw new ResponseStatusException(
                UNPROCESSABLE_ENTITY, "Já existe uma conta cadastrada com o CPF informado."
            );
        }

        conta = contaRepository.save(conta);
        logger.info("{} cadastrada com sucesso.", conta);
        URI location = uriBuilder.path("/contas/{id}").buildAndExpand(conta.getId()).toUri();

        return ResponseEntity.created(location).build();
    }

}
