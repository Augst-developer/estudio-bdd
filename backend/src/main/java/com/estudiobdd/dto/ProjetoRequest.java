package com.estudiobdd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjetoRequest(
        @NotBlank(message = "O nome do projeto é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres.")
        String descricao,

        @Size(max = 10000, message = "O contexto do domínio deve ter no máximo 10000 caracteres.")
        String contextoDominio) {
}
