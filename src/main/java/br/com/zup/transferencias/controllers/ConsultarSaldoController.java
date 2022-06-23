package br.com.zup.transferencias.controllers;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.zup.transferencias.controllers.responses.ConsultarSaldoResponse;
import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.repositories.ContaRepository;

@RestController
@RequestMapping("/contas/{id}")
public class ConsultarSaldoController {

    Logger logger = LoggerFactory.getLogger(ConsultarSaldoController.class);

    private final ContaRepository contaRepository;

    public ConsultarSaldoController(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @GetMapping
    public ResponseEntity<?> consultar(@PathVariable Long id) {
        Conta conta = contaRepository.findById(id)
                                     .orElseThrow(
                                         () -> new ResponseStatusException(
                                             NOT_FOUND, "Não existe uma conta com o id informado."
                                         )
                                     );

        logger.info("Saldo da {} consultado com sucesso.", conta);
        return ResponseEntity.ok(new ConsultarSaldoResponse(conta));
    }

}
