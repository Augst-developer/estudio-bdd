package com.estudiobdd.service;

import com.estudiobdd.dto.HistoriaResumoResponse;
import com.estudiobdd.dto.Mapper;
import com.estudiobdd.dto.ResumoProjetoResponse;
import com.estudiobdd.dto.ResumoProjetoResponse.MotivoFrequencia;
import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.MotivoRejeicao;
import com.estudiobdd.model.OrigemCenario;
import com.estudiobdd.model.Prioridade;
import com.estudiobdd.model.Projeto;
import com.estudiobdd.repository.CenarioRepository;
import com.estudiobdd.repository.HistoriaUsuarioRepository;
import com.estudiobdd.repository.SugestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Regras 6 e 7: consulta do projeto (com filtro por prioridade) e resumo. */
@Service
@Transactional(readOnly = true)
public class ConsultaProjetoService {

    private final ProjetoService projetoService;
    private final HistoriaUsuarioRepository historiaRepository;
    private final CenarioRepository cenarioRepository;
    private final SugestaoRepository sugestaoRepository;

    public ConsultaProjetoService(ProjetoService projetoService,
                                  HistoriaUsuarioRepository historiaRepository,
                                  CenarioRepository cenarioRepository,
                                  SugestaoRepository sugestaoRepository) {
        this.projetoService = projetoService;
        this.historiaRepository = historiaRepository;
        this.cenarioRepository = cenarioRepository;
        this.sugestaoRepository = sugestaoRepository;
    }

    public List<HistoriaResumoResponse> consultar(Long projetoId, Prioridade prioridade) {
        projetoService.buscar(projetoId);
        List<HistoriaUsuario> historias = prioridade == null
                ? historiaRepository.findByProjetoIdOrderByIdAsc(projetoId)
                : historiaRepository.findByProjetoIdAndPrioridadeOrderByIdAsc(projetoId, prioridade);

        // Duas consultas agregadas para todo o projeto, em vez de uma contagem por história.
        Map<Long, Long> cenarios = paraMapa(cenarioRepository.contarPorHistoria(projetoId));
        Map<Long, Long> pendentes = paraMapa(
                sugestaoRepository.contarPorHistoriaEDecisao(projetoId, DecisaoSugestao.PENDENTE));

        return historias.stream()
                .map(h -> Mapper.historiaResumo(h, cenarios.getOrDefault(h.getId(), 0L),
                        pendentes.getOrDefault(h.getId(), 0L)))
                .toList();
    }

    public ResumoProjetoResponse resumo(Long projetoId) {
        Projeto projeto = projetoService.buscar(projetoId);

        Map<OrigemCenario, Long> porOrigem = new EnumMap<>(OrigemCenario.class);
        for (OrigemCenario o : OrigemCenario.values()) {
            porOrigem.put(o, 0L);
        }
        long totalCenarios = 0;
        for (Object[] linha : cenarioRepository.contarPorOrigem(projetoId)) {
            long qtd = (Long) linha[1];
            porOrigem.put((OrigemCenario) linha[0], qtd);
            totalCenarios += qtd;
        }

        Map<DecisaoSugestao, Long> porDecisao = new EnumMap<>(DecisaoSugestao.class);
        for (DecisaoSugestao d : DecisaoSugestao.values()) {
            porDecisao.put(d, 0L);
        }
        for (Object[] linha : sugestaoRepository.contarPorDecisao(projetoId)) {
            porDecisao.put((DecisaoSugestao) linha[0], (Long) linha[1]);
        }

        List<MotivoFrequencia> motivos = sugestaoRepository
                .contarMotivosRejeicao(projetoId, DecisaoSugestao.REJEITADA).stream()
                .map(l -> new MotivoFrequencia((MotivoRejeicao) l[0], (Long) l[1]))
                .toList();

        return new ResumoProjetoResponse(projeto.getId(), projeto.getNome(),
                historiaRepository.countByProjetoId(projetoId), totalCenarios, porOrigem,
                porDecisao, motivos);
    }

    private static Map<Long, Long> paraMapa(List<Object[]> linhas) {
        Map<Long, Long> mapa = new HashMap<>();
        for (Object[] l : linhas) {
            mapa.put((Long) l[0], (Long) l[1]);
        }
        return mapa;
    }
}
