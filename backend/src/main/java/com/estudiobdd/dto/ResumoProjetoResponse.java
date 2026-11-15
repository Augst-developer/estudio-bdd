package com.estudiobdd.dto;

import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.model.MotivoRejeicao;
import com.estudiobdd.model.OrigemCenario;

import java.util.List;
import java.util.Map;

public record ResumoProjetoResponse(Long projetoId, String projetoNome, long totalHistorias,
                                    long totalCenarios,
                                    Map<OrigemCenario, Long> cenariosPorOrigem,
                                    Map<DecisaoSugestao, Long> sugestoesPorDecisao,
                                    List<MotivoFrequencia> motivosRejeicaoMaisFrequentes) {

    public record MotivoFrequencia(MotivoRejeicao motivo, long quantidade) {
    }
}
