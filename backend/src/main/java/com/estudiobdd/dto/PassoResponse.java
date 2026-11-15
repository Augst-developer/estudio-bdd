package com.estudiobdd.dto;

import com.estudiobdd.model.TipoPasso;

public record PassoResponse(Long id, TipoPasso tipo, String texto, int posicao) {
}
