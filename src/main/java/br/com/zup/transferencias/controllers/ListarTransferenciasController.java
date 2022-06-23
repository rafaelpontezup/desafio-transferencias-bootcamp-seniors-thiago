package br.com.zup.transferencias.controllers;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.zup.transferencias.controllers.responses.ListarTransferenciasResponse;
import br.com.zup.transferencias.models.Conta;
import br.com.zup.transferencias.repositories.ContaRepository;
import br.com.zup.transferencias.repositories.TransferenciaRepository;

@RestController
@RequestMapping("/contas/{id}/transferencias")
public class ListarTransferenciasController {

    Logger logger = LoggerFactory.getLogger(ListarTransferenciasController.class);

    private final ContaRepository contaRepository;
    private final TransferenciaRepository transferenciaRepository;

    public ListarTransferenciasController(ContaRepository contaRepository,
                                          TransferenciaRepository transferenciaRepository) {
        this.contaRepository = contaRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @GetMapping
    public ResponseEntity<?> listar(@PathVariable Long id,
                                    @PageableDefault(sort = "id", direction = Direction.ASC, page = 0, size = 2) Pageable paginacao) {
        Conta conta = contaRepository.findById(id)
                                     .orElseThrow(
                                         () -> new ResponseStatusException(
                                             NOT_FOUND, "Não existe uma conta com o id informado."
                                         )
                                     );

        Page<ListarTransferenciasResponse> response = transferenciaRepository.findByOrigemIdOrDestinoId(
            conta.getId(), conta.getId(), paginacao
        ).map(t -> new ListarTransferenciasResponse(conta, t));

        logger.info("Transferências da {} listadas com sucesso.", conta);
        return ResponseEntity.ok(response);
    }

}
