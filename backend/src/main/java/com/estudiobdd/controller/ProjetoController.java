package com.estudiobdd.controller;

import com.estudiobdd.dto.HistoriaRequest;
import com.estudiobdd.dto.HistoriaResponse;
import com.estudiobdd.dto.HistoriaResumoResponse;
import com.estudiobdd.dto.ProjetoRequest;
import com.estudiobdd.dto.ProjetoResponse;
import com.estudiobdd.dto.ResumoProjetoResponse;
import com.estudiobdd.model.Prioridade;
import com.estudiobdd.service.ConsultaProjetoService;
import com.estudiobdd.service.HistoriaService;
import com.estudiobdd.service.ProjetoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projetos")
public class ProjetoController {

    static final String CABECALHO_USUARIO = "X-Usuario-Id";

    private final ProjetoService projetoService;
    private final HistoriaService historiaService;
    private final ConsultaProjetoService consultaService;

    public ProjetoController(ProjetoService projetoService, HistoriaService historiaService,
                             ConsultaProjetoService consultaService) {
        this.projetoService = projetoService;
        this.historiaService = historiaService;
        this.consultaService = consultaService;
    }

    @GetMapping
    public List<ProjetoResponse> listar() {
        return projetoService.listar();
    }

    @PostMapping
    public ResponseEntity<ProjetoResponse> criar(
            @RequestHeader(name = CABECALHO_USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody ProjetoRequest request) {
        ProjetoResponse criado = projetoService.criar(usuarioId, request);
        return ResponseEntity.created(URI.create("/api/projetos/" + criado.id())).body(criado);
    }

    @GetMapping("/{id}")
    public ProjetoResponse obter(@PathVariable Long id) {
        return projetoService.obter(id);
    }

    @PutMapping("/{id}")
    public ProjetoResponse editar(@PathVariable Long id,
            @RequestHeader(name = CABECALHO_USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody ProjetoRequest request) {
        return projetoService.editar(id, usuarioId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(name = CABECALHO_USUARIO, required = false) Long usuarioId) {
        projetoService.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /** Consulta do projeto: histórias com quantidade de cenários e de sugestões pendentes. */
    @GetMapping("/{id}/historias")
    public List<HistoriaResumoResponse> historias(@PathVariable Long id,
            @RequestParam(required = false) Prioridade prioridade) {
        return consultaService.consultar(id, prioridade);
    }

    @PostMapping("/{id}/historias")
    public ResponseEntity<HistoriaResponse> criarHistoria(@PathVariable Long id,
            @RequestHeader(name = CABECALHO_USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody HistoriaRequest request) {
        HistoriaResponse criada = historiaService.criar(id, usuarioId, request);
        return ResponseEntity.created(URI.create("/api/historias/" + criada.id())).body(criada);
    }

    @GetMapping("/{id}/resumo")
    public ResumoProjetoResponse resumo(@PathVariable Long id) {
        return consultaService.resumo(id);
    }
}
