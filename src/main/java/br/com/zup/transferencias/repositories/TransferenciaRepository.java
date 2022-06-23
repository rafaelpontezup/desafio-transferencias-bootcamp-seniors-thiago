package br.com.zup.transferencias.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.zup.transferencias.models.Transferencia;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    Integer countByOrigemId(Long origemId);

    Integer countByDestinoId(Long destinoId);

    Page<Transferencia> findByOrigemIdOrDestinoId(Long origemId, Long destinoId,
                                                  Pageable paginacao);

}
