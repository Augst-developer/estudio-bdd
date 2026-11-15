package com.estudiobdd.repository;

import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.Prioridade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HistoriaUsuarioRepository extends JpaRepository<HistoriaUsuario, Long> {

    @EntityGraph(attributePaths = "autor")
    List<HistoriaUsuario> findByProjetoIdOrderByIdAsc(Long projetoId);

    @EntityGraph(attributePaths = "autor")
    List<HistoriaUsuario> findByProjetoIdAndPrioridadeOrderByIdAsc(Long projetoId, Prioridade prioridade);

    Optional<HistoriaUsuario> findByProjetoIdAndTituloIgnoreCase(Long projetoId, String titulo);

    long countByProjetoId(Long projetoId);
}
