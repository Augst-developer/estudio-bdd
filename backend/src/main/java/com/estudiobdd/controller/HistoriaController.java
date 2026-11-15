package com.estudiobdd.controller;

import com.estudiobdd.dto.ArquivoFeature;
import com.estudiobdd.dto.CenarioRequest;
import com.estudiobdd.dto.CenarioResponse;
import com.estudiobdd.dto.HistoriaRequest;
import com.estudiobdd.dto.HistoriaResponse;
import com.estudiobdd.dto.SugestaoRequest;
import com.estudiobdd.dto.SugestaoResponse;
import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.service.CenarioService;
import com.estudiobdd.service.ExportacaoService;
import com.estudiobdd.service.HistoriaService;
import com.estudiobdd.service.SugestaoService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/historias")
public class HistoriaController {

    private static final String USUARIO = ProjetoController.CABECALHO_USUARIO;

    private final HistoriaService historiaService;
    private final CenarioService cenarioService;
    private final SugestaoService sugestaoService;
    private final ExportacaoService exportacaoService;

    public HistoriaController(HistoriaService historiaService, CenarioService cenarioService,
                              SugestaoService sugestaoService, ExportacaoService exportacaoService) {
        this.historiaService = historiaService;
        this.cenarioService = cenarioService;
        this.sugestaoService = sugestaoService;
        this.exportacaoService = exportacaoService;
    }

    @GetMapping("/{id}")
    public HistoriaResponse obter(@PathVariable Long id) {
        return historiaService.obter(id);
    }

    @PutMapping("/{id}")
    public HistoriaResponse editar(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody HistoriaRequest request) {
        return historiaService.editar(id, usuarioId, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId) {
        historiaService.excluir(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    // ---- cenários ----

    @GetMapping("/{id}/cenarios")
    public List<CenarioResponse> cenarios(@PathVariable Long id) {
        return cenarioService.listar(id);
    }

    @PostMapping("/{id}/cenarios")
    public ResponseEntity<CenarioResponse> criarCenario(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody CenarioRequest request) {
        CenarioResponse criado = cenarioService.criar(id, usuarioId, request);
        return ResponseEntity.created(URI.create("/api/cenarios/" + criado.id())).body(criado);
    }

    // ---- sugestões ----

    @GetMapping("/{id}/sugestoes")
    public List<SugestaoResponse> sugestoes(@PathVariable Long id,
            @RequestParam(required = false) DecisaoSugestao decisao) {
        return sugestaoService.listar(id, decisao);
    }

    @PostMapping("/{id}/sugestoes")
    public ResponseEntity<SugestaoResponse> registrarSugestao(@PathVariable Long id,
            @RequestHeader(name = USUARIO, required = false) Long usuarioId,
            @Valid @RequestBody SugestaoRequest request) {
        SugestaoResponse criada = sugestaoService.registrar(id, usuarioId, request);
        return ResponseEntity.created(URI.create("/api/sugestoes/" + criada.id())).body(criada);
    }

    // ---- exportação ----

    @GetMapping(value = "/{id}/feature", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> exportar(@PathVariable Long id) {
        ArquivoFeature arquivo = exportacaoService.exportar(id);
        ContentDisposition disposicao = ContentDisposition.attachment()
                .filename(arquivo.nome(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .contentType(new MediaType("text", "plain", StandardCharsets.UTF_8))
                .body(arquivo.conteudo());
    }
}
