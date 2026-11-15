package com.estudiobdd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SugestaoRequest(
        @NotBlank(message = "A ferramenta de IA é obrigatória.")
        @Size(max = 120, message = "A ferramenta deve ter no máximo 120 caracteres.")
        String ferramenta,

        @NotBlank(message = "O prompt utilizado é obrigatório.")
        @Size(max = 20000, message = "O prompt deve ter no máximo 20000 caracteres.")
        String prompt,

        @NotBlank(message = "O conteúdo da sugestão é obrigatório.")
        @Size(max = 20000, message = "O conteúdo deve ter no máximo 20000 caracteres.")
        String conteudo) {
}
