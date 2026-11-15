package com.estudiobdd.dto;

import com.estudiobdd.model.OrigemCenario;

import java.util.List;

public record CenarioResponse(Long id, Long historiaId, String titulo, int posicao,
                              OrigemCenario origem, List<PassoResponse> passos) {
}
