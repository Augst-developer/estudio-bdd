package com.estudiobdd.dto;

import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.model.MotivoRejeicao;

import java.time.LocalDateTime;

public record SugestaoResponse(Long id, Long historiaId, String ferramenta, String prompt,
                               String conteudo, DecisaoSugestao decisao,
                               MotivoRejeicao motivoRejeicao, String justificativa,
                               UsuarioResponse registradaPor, UsuarioResponse decididaPor,
                               LocalDateTime registradoEm, LocalDateTime decididoEm) {
}
