package com.estudiobdd.dto;

import com.estudiobdd.model.Prioridade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HistoriaRequest(
        @NotBlank(message = "O título é obrigatório.")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres.")
        String titulo,

        @NotBlank(message = "O papel (Como...) é obrigatório.")
        @Size(max = 500, message = "O papel deve ter no máximo 500 caracteres.")
        String papel,

        @NotBlank(message = "O objetivo (quero...) é obrigatório.")
        @Size(max = 1000, message = "O objetivo deve ter no máximo 1000 caracteres.")
        String objetivo,

        @NotBlank(message = "O benefício (para...) é obrigatório.")
        @Size(max = 1000, message = "O benefício deve ter no máximo 1000 caracteres.")
        String beneficio,

        @NotNull(message = "A prioridade é obrigatória (ALTA, MEDIA ou BAIXA).")
        Prioridade prioridade) {
}
