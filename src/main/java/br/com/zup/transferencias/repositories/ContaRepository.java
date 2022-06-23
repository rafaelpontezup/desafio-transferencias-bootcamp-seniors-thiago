package br.com.zup.transferencias.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.zup.transferencias.models.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    boolean existsByEmail(String email);

    boolean existsByCpf_Hash(byte[] hash);

}
