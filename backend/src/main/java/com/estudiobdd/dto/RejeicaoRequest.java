package com.estudiobdd.dto;

import com.estudiobdd.model.MotivoRejeicao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RejeicaoRequest(
        @NotNull(message = "A rejeição exige um motivo (INCORRETA, GENERICA, DUPLICADA, FORA_DO_ESCOPO ou OUTRO).")
        MotivoRejeicao motivo,

        @Size(max = 5000, message = "A justificativa deve ter no máximo 5000 caracteres.")
        String justificativa) {
}
