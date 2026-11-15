package com.estudiobdd.dto;

import java.time.LocalDateTime;

public record ProjetoResponse(Long id, String nome, String descricao, String contextoDominio,
                              LocalDateTime dataCriacao) {
}
