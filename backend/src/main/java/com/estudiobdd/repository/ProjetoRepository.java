package com.estudiobdd.repository;

import com.estudiobdd.model.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    Optional<Projeto> findByNomeIgnoreCase(String nome);
}
