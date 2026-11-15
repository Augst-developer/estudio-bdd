package com.estudiobdd.service;

import com.estudiobdd.dto.CenarioRequest;
import com.estudiobdd.dto.PassoRequest;
import com.estudiobdd.dto.RejeicaoRequest;
import com.estudiobdd.exception.AcessoNegadoException;
import com.estudiobdd.exception.ConflitoException;
import com.estudiobdd.exception.RegraDeNegocioException;
import com.estudiobdd.model.Cenario;
import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.MotivoRejeicao;
import com.estudiobdd.model.OrigemCenario;
import com.estudiobdd.model.Passo;
import com.estudiobdd.model.Prioridade;
import com.estudiobdd.model.Projeto;
import com.estudiobdd.model.Sugestao;
import com.estudiobdd.model.TipoPasso;
import com.estudiobdd.model.Usuario;
import com.estudiobdd.repository.CenarioRepository;
import com.estudiobdd.repository.PassoRepository;
import com.estudiobdd.repository.SugestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes de regras de negócio dos serviços, sem banco de dados (repositórios simulados). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegrasDeNegocioTest {

    @Mock UsuarioService usuarioService;
    @Mock HistoriaService historiaService;
    @Mock CenarioRepository cenarioRepository;
    @Mock PassoRepository passoRepository;
    @Mock SugestaoRepository sugestaoRepository;

    Usuario autor;
    Usuario outro;
    HistoriaUsuario historia;

    @BeforeEach
    void preparar() {
        autor = usuario(1L, "Ana");
        outro = usuario(2L, "Bruno");
        Projeto projeto = new Projeto("Projeto", "desc", "ctx");
        ReflectionTestUtils.setField(projeto, "id", 10L);
        historia = new HistoriaUsuario(projeto, autor, "Título", "cliente", "comprar", "receber",
                Prioridade.ALTA);
        ReflectionTestUtils.setField(historia, "id", 100L);

        when(usuarioService.obterAtual(1L)).thenReturn(autor);
        when(usuarioService.obterAtual(2L)).thenReturn(outro);
        when(historiaService.buscar(100L)).thenReturn(historia);
    }

    private static Usuario usuario(Long id, String nome) {
        Usuario u = new Usuario(nome, nome.toLowerCase() + "@exemplo.com");
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private CenarioService cenarioService() {
        return new CenarioService(cenarioRepository, passoRepository, historiaService, usuarioService);
    }

    private SugestaoService sugestaoService() {
        return new SugestaoService(sugestaoRepository, historiaService, usuarioService);
    }

    private static CenarioRequest cenario(String titulo, TipoPasso... tipos) {
        List<PassoRequest> passos = java.util.Arrays.stream(tipos)
                .map(t -> new PassoRequest(t, "texto " + t, null)).toList();
        return new CenarioRequest(titulo, OrigemCenario.HUMANO, null, passos);
    }

    // ---- Teste 1: cenário sem passo Então é rejeitado ----

    @Test
    void cenarioSemPassoEntaoERejeitado() {
        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () ->
                cenarioService().criar(100L, 1L, cenario("Sem então", TipoPasso.DADO, TipoPasso.QUANDO)));

        assertTrue(ex.getMessage().contains("Então"));
        verify(cenarioRepository, never()).save(any());
    }

    @Test
    void cenarioValidoEGravadoComPassosNaOrdemInformada() {
        var resposta = cenarioService().criar(100L, 1L, cenario("Válido",
                TipoPasso.DADO, TipoPasso.QUANDO, TipoPasso.ENTAO, TipoPasso.E));

        assertEquals(4, resposta.passos().size());
        assertEquals(List.of(1, 2, 3, 4), resposta.passos().stream().map(p -> p.posicao()).toList());
        assertEquals(1, resposta.posicao());
    }

    @Test
    void somenteOAutorAlteraCenarios() {
        assertThrows(AcessoNegadoException.class, () ->
                cenarioService().criar(100L, 2L, cenario("X", TipoPasso.QUANDO, TipoPasso.ENTAO)));
    }

    @Test
    void tituloDeCenarioRepetidoNaHistoriaEConflito() {
        historia.adicionarCenario(new Cenario("Repetido", OrigemCenario.HUMANO));
        assertThrows(ConflitoException.class, () ->
                cenarioService().criar(100L, 1L, cenario("repetido", TipoPasso.QUANDO, TipoPasso.ENTAO)));
    }

    // ---- Teste 2: rejeição de sugestão sem motivo ----

    @Test
    void rejeicaoSemMotivoENegadaEASugestaoContinuaPendente() {
        Sugestao sugestao = new Sugestao(historia, autor, "IA", "prompt", "conteúdo");
        when(sugestaoRepository.findById(5L)).thenReturn(Optional.of(sugestao));

        assertThrows(RegraDeNegocioException.class, () ->
                sugestaoService().rejeitar(5L, 1L, new RejeicaoRequest(null, "porque sim")));

        assertEquals(DecisaoSugestao.PENDENTE, sugestao.getDecisao());
    }

    @Test
    void rejeicaoComMotivoRegistraDecisaoEDecisor() {
        Sugestao sugestao = new Sugestao(historia, autor, "IA", "prompt", "conteúdo");
        when(sugestaoRepository.findById(5L)).thenReturn(Optional.of(sugestao));

        var resposta = sugestaoService().rejeitar(5L, 2L,
                new RejeicaoRequest(MotivoRejeicao.GENERICA, "vago"));

        assertEquals(DecisaoSugestao.REJEITADA, resposta.decisao());
        assertEquals(MotivoRejeicao.GENERICA, resposta.motivoRejeicao());
        assertEquals(2L, resposta.decididaPor().id());
    }

    @Test
    void sugestaoJaDecididaNaoPodeSerAlterada() {
        Sugestao sugestao = new Sugestao(historia, autor, "IA", "prompt", "conteúdo");
        sugestao.aceitar(autor, "ok");
        when(sugestaoRepository.findById(5L)).thenReturn(Optional.of(sugestao));

        assertThrows(ConflitoException.class, () ->
                sugestaoService().rejeitar(5L, 1L, new RejeicaoRequest(MotivoRejeicao.OUTRO, null)));
        assertThrows(ConflitoException.class, () -> sugestaoService().aceitar(5L, 1L, null));
        assertEquals(DecisaoSugestao.ACEITA, sugestao.getDecisao());
    }

    // ---- Teste 3: exportação bloqueada com sugestão pendente ----

    @Test
    void exportacaoBloqueadaComSugestaoPendente() {
        historia.adicionarCenario(cenarioValido("Cenário 1"));
        when(sugestaoRepository.countByHistoriaIdAndDecisao(100L, DecisaoSugestao.PENDENTE)).thenReturn(1L);

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () ->
                new ExportacaoService(historiaService, sugestaoRepository).exportar(100L));
        assertTrue(ex.getMessage().contains("pendente"));
    }

    @Test
    void exportacaoBloqueadaSemCenarios() {
        assertThrows(RegraDeNegocioException.class, () ->
                new ExportacaoService(historiaService, sugestaoRepository).exportar(100L));
    }

    @Test
    void exportacaoGeraGherkinEmPortugues() {
        historia.adicionarCenario(cenarioValido("Cenário 1"));
        when(sugestaoRepository.countByHistoriaIdAndDecisao(100L, DecisaoSugestao.PENDENTE)).thenReturn(0L);

        var arquivo = new ExportacaoService(historiaService, sugestaoRepository).exportar(100L);

        assertEquals("titulo.feature", arquivo.nome());
        assertEquals("""
                # language: pt
                Funcionalidade: Título
                  Como cliente
                  Quero comprar
                  Para receber

                  Cenário: Cenário 1
                    Dado que existe um item
                    Quando eu comprar
                    Então recebo
                    E vejo o recibo
                """, arquivo.conteudo());
    }

    private static Cenario cenarioValido(String titulo) {
        Cenario c = new Cenario(titulo, OrigemCenario.HUMANO);
        c.substituirPassos(List.of(
                new Passo(TipoPasso.DADO, "que existe um item"),
                new Passo(TipoPasso.QUANDO, "eu comprar"),
                new Passo(TipoPasso.ENTAO, "recebo"),
                new Passo(TipoPasso.E, "vejo o recibo")));
        return c;
    }
}
