package com.estudiobdd.repository;

import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.model.Sugestao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SugestaoRepository extends JpaRepository<Sugestao, Long> {

    @EntityGraph(attributePaths = {"registradaPor", "decididaPor"})
    List<Sugestao> findByHistoriaIdOrderByIdAsc(Long historiaId);

    @EntityGraph(attributePaths = {"registradaPor", "decididaPor"})
    List<Sugestao> findByHistoriaIdAndDecisaoOrderByIdAsc(Long historiaId, DecisaoSugestao decisao);

    long countByHistoriaIdAndDecisao(Long historiaId, DecisaoSugestao decisao);

    /** Linhas [historiaId, quantidade] das sugestões de um projeto com a decisão informada. */
    @Query("select s.historia.id, count(s) from Sugestao s "
            + "where s.historia.projeto.id = :projetoId and s.decisao = :decisao "
            + "group by s.historia.id")
    List<Object[]> contarPorHistoriaEDecisao(@Param("projetoId") Long projetoId,
                                              @Param("decisao") DecisaoSugestao decisao);

    /** Linhas [decisao, quantidade] das sugestões de um projeto. */
    @Query("select s.decisao, count(s) from Sugestao s "
            + "where s.historia.projeto.id = :projetoId group by s.decisao")
    List<Object[]> contarPorDecisao(@Param("projetoId") Long projetoId);

    /** Linhas [motivo, quantidade] das sugestões rejeitadas, da mais frequente à menos frequente. */
    @Query("select s.motivoRejeicao, count(s) from Sugestao s "
            + "where s.historia.projeto.id = :projetoId and s.decisao = :decisao "
            + "and s.motivoRejeicao is not null "
            + "group by s.motivoRejeicao order by count(s) desc")
    List<Object[]> contarMotivosRejeicao(@Param("projetoId") Long projetoId,
                                          @Param("decisao") DecisaoSugestao decisao);
}
