package com.estudiobdd.service;

import com.estudiobdd.dto.ArquivoFeature;
import com.estudiobdd.exception.RegraDeNegocioException;
import com.estudiobdd.model.Cenario;
import com.estudiobdd.model.DecisaoSugestao;
import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.Passo;
import com.estudiobdd.repository.SugestaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

/** Regra 5: exporta em Gherkin (# language: pt) somente com cenários e sem sugestões pendentes. */
@Service
@Transactional(readOnly = true)
public class ExportacaoService {

    private final HistoriaService historiaService;
    private final SugestaoRepository sugestaoRepository;

    public ExportacaoService(HistoriaService historiaService, SugestaoRepository sugestaoRepository) {
        this.historiaService = historiaService;
        this.sugestaoRepository = sugestaoRepository;
    }

    public ArquivoFeature exportar(Long historiaId) {
        HistoriaUsuario historia = historiaService.buscar(historiaId);
        if (historia.getCenarios().isEmpty()) {
            throw new RegraDeNegocioException(
                    "A história não pode ser exportada: não possui nenhum cenário.");
        }
        long pendentes = sugestaoRepository.countByHistoriaIdAndDecisao(
                historiaId, DecisaoSugestao.PENDENTE);
        if (pendentes > 0) {
            throw new RegraDeNegocioException("A história não pode ser exportada: possui "
                    + pendentes + " sugestão(ões) pendente(s).");
        }
        return new ArquivoFeature(nomeDoArquivo(historia), gerar(historia));
    }

    static String gerar(HistoriaUsuario h) {
        StringBuilder sb = new StringBuilder();
        sb.append("# language: pt\n");
        sb.append("Funcionalidade: ").append(h.getTitulo()).append('\n');
        sb.append("  Como ").append(h.getPapel()).append('\n');
        sb.append("  Quero ").append(h.getObjetivo()).append('\n');
        sb.append("  Para ").append(h.getBeneficio()).append('\n');
        for (Cenario c : h.getCenarios()) {
            sb.append('\n');
            sb.append("  Cenário: ").append(c.getTitulo()).append('\n');
            for (Passo p : c.getPassos()) {
                sb.append("    ").append(p.getTipo().palavraChave()).append(' ')
                        .append(p.getTexto()).append('\n');
            }
        }
        return sb.toString();
    }

    private static String nomeDoArquivo(HistoriaUsuario h) {
        String base = Normalizer.normalize(h.getTitulo(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.isEmpty()) {
            base = "historia-" + h.getId();
        }
        return base + ".feature";
    }
}
