package com.estudiobdd.dto;

import com.estudiobdd.model.Prioridade;

/** Linha da consulta do projeto: história com a quantidade de cenários e de sugestões pendentes. */
public record HistoriaResumoResponse(Long id, String titulo, Prioridade prioridade, Long autorId,
                                     String autorNome, long quantidadeCenarios,
                                     long sugestoesPendentes) {
}
