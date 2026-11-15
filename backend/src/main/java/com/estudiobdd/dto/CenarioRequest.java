package com.estudiobdd.dto;

import com.estudiobdd.model.OrigemCenario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CenarioRequest(
        @NotBlank(message = "O título do cenário é obrigatório.")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres.")
        String titulo,

        /** Opcional; o padrão é HUMANO. */
        OrigemCenario origem,

        /** Opcional; o padrão é o final da lista. */
        @Min(value = 1, message = "A posição deve ser maior ou igual a 1.")
        Integer posicao,

        @NotEmpty(message = "O cenário deve ter ao menos um passo.")
        @Valid
        List<PassoRequest> passos) {
}
