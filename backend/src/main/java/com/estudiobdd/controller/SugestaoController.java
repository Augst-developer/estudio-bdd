package com.estudiobdd.controller;

import com.estudiobdd.dto.AceiteRequest;
import com.estudiobdd.dto.RejeicaoRequest;
import com.estudiobdd.dto.SugestaoResponse;
import com.estudiobdd.service.SugestaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sugestoes")
public class SugestaoController {

    private static final String USUARIO = ProjetoController.CABECALHO_USUARIO;

    private final SugestaoService sugestaoService;

    public SugestaoController(SugestaoService sugestaoService) {
        this.sugestaoService = sugestaoService;
    }

    @PostMapping("/{id}/aceitar")
    public SugestaoResponse aceitar(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody(required = false) AceiteRequest request) {
        return sugestaoService.aceitar(id, usuarioId, request);
    }

    @PostMapping("/{id}/rejeitar")
    public SugestaoResponse rejeitar(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody RejeicaoRequest request) {
        return sugestaoService.rejeitar(id, usuarioId, request);
    }
}
