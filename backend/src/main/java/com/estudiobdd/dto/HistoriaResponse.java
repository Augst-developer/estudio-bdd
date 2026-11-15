package com.estudiobdd.dto;

import com.estudiobdd.model.Prioridade;

import java.time.LocalDateTime;
import java.util.List;

public record HistoriaResponse(Long id, Long projetoId, String titulo, String papel, String objetivo,
                               String beneficio, Prioridade prioridade, UsuarioResponse autor,
                               LocalDateTime criadoEm, LocalDateTime atualizadoEm,
                               long sugestoesPendentes, List<CenarioResponse> cenarios) {
}
