package com.estudiobdd.repository;

import com.estudiobdd.model.Cenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CenarioRepository extends JpaRepository<Cenario, Long> {

    /** Linhas [historiaId, quantidade] dos cenários de um projeto. */
    @Query("select c.historia.id, count(c) from Cenario c "
            + "where c.historia.projeto.id = :projetoId group by c.historia.id")
    List<Object[]> contarPorHistoria(@Param("projetoId") Long projetoId);

    /** Linhas [origem, quantidade] dos cenários de um projeto. */
    @Query("select c.origem, count(c) from Cenario c "
            + "where c.historia.projeto.id = :projetoId group by c.origem")
    List<Object[]> contarPorOrigem(@Param("projetoId") Long projetoId);
}
