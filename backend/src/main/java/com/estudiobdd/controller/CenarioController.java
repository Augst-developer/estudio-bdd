package com.estudiobdd.controller;

import com.estudiobdd.dto.CenarioRequest;
import com.estudiobdd.dto.CenarioResponse;
import com.estudiobdd.dto.PassoRequest;
import com.estudiobdd.dto.PassoResponse;
import com.estudiobdd.service.CenarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api")
public class CenarioController {

    private static final String USUARIO = ProjetoController.CABECALHO_USUARIO;

    private final CenarioService cenarioService;

    public CenarioController(CenarioService cenarioService) {
        this.cenarioService = cenarioService;
    }

    @GetMapping("/cenarios/{id}")
    public CenarioResponse obter(@PathVariable Long id) {
        return cenarioService.obter(id);
    }

    @PutMapping("/cenarios/{id}")
    public CenarioResponse substituir(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody CenarioRequest request) {
        return cenarioService.substituir(id, usuarioId, request);
    }

    @DeleteMapping("/cenarios/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId) {
        cenarioService.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cenarios/{id}/passos")
    public ResponseEntity<PassoResponse> adicionarPasso(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody PassoRequest request) {
        PassoResponse criado = cenarioService.adicionarPasso(id, usuarioId, request);
        return ResponseEntity.created(URI.create("/api/cenarios/" + id)).body(criado);
    }

    @PutMapping("/passos/{id}")
    public PassoResponse editarPasso(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody PassoRequest request) {
        return cenarioService.editarPasso(id, usuarioId, request);
    }

    @DeleteMapping("/passos/{id}")
    public ResponseEntity<Void> excluirPasso(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId) {
        cenarioService.excluirPasso(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
