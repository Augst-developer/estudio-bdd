package com.estudiobdd.dto;

import com.estudiobdd.model.TipoPasso;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A posição só é usada nas operações sobre um passo isolado; ao cadastrar ou substituir um
 * cenário, a ordem da lista define as posições.
 */
public record PassoRequest(
        @NotNull(message = "O tipo do passo é obrigatório (DADO, QUANDO, ENTAO, E ou MAS).")
        TipoPasso tipo,

        @NotBlank(message = "O texto do passo é obrigatório.")
        @Size(max = 500, message = "O texto do passo deve ter no máximo 500 caracteres.")
        String texto,

        @Min(value = 1, message = "A posição deve ser maior ou igual a 1.")
        Integer posicao) {
}
