package com.estudiobdd.config;

import com.estudiobdd.model.Cenario;
import com.estudiobdd.model.HistoriaUsuario;
import com.estudiobdd.model.MotivoRejeicao;
import com.estudiobdd.model.OrigemCenario;
import com.estudiobdd.model.Passo;
import com.estudiobdd.model.Prioridade;
import com.estudiobdd.model.Projeto;
import com.estudiobdd.model.Sugestao;
import com.estudiobdd.model.TipoPasso;
import com.estudiobdd.model.Usuario;
import com.estudiobdd.repository.HistoriaUsuarioRepository;
import com.estudiobdd.repository.ProjetoRepository;
import com.estudiobdd.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Dados fictícios de demonstração. Só são carregados se o banco estiver vazio. */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ProjetoRepository projetoRepository;
    private final HistoriaUsuarioRepository historiaRepository;

    public DadosIniciais(UsuarioRepository usuarioRepository, ProjetoRepository projetoRepository,
                         HistoriaUsuarioRepository historiaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.projetoRepository = projetoRepository;
        this.historiaRepository = historiaRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        Usuario ana = usuarioRepository.save(new Usuario("Ana Analista", "ana@exemplo.com"));
        Usuario bruno = usuarioRepository.save(new Usuario("Bruno Revisor", "bruno@exemplo.com"));

        Projeto loja = projetoRepository.save(new Projeto("Loja Virtual",
                "Sistema de vendas online de uma loja fictícia.",
                "Glossário: carrinho = itens escolhidos antes do pagamento; pedido = compra confirmada."));
        projetoRepository.save(new Projeto("Biblioteca Escolar",
                "Controle de empréstimos de livros de uma escola fictícia.",
                "Glossário: exemplar = cópia física de um livro; empréstimo dura 7 dias."));

        // História 1: pronta para exportar (com cenários e sugestões já decididas)
        HistoriaUsuario h1 = new HistoriaUsuario(loja, ana, "Adicionar produto ao carrinho",
                "cliente da loja", "adicionar um produto ao carrinho",
                "reunir os itens que pretendo comprar", Prioridade.ALTA);
        h1.adicionarCenario(cenario("Adicionar produto disponível", OrigemCenario.HUMANO,
                passo(TipoPasso.DADO, "que o produto \"Caneca\" está em estoque"),
                passo(TipoPasso.QUANDO, "eu adiciono a \"Caneca\" ao carrinho"),
                passo(TipoPasso.ENTAO, "o carrinho passa a ter 1 item"),
                passo(TipoPasso.E, "o total do carrinho é atualizado")));
        h1.adicionarCenario(cenario("Produto sem estoque", OrigemCenario.SUGESTAO_IA,
                passo(TipoPasso.DADO, "que o produto \"Caneca\" está sem estoque"),
                passo(TipoPasso.QUANDO, "eu tento adicionar a \"Caneca\" ao carrinho"),
                passo(TipoPasso.ENTAO, "vejo a mensagem \"Produto indisponível\""),
                passo(TipoPasso.MAS, "o carrinho não é alterado")));
        Sugestao s1 = new Sugestao(h1, ana, "Assistente de IA (exemplo)",
                "Sugira cenários de borda para adicionar produto ao carrinho.",
                "Cenário: produto sem estoque. Quando tento adicionar, então vejo aviso de indisponibilidade.");
        s1.aceitar(ana, "Cobre um caso de borda relevante; cenário incluído manualmente.");
        Sugestao s2 = new Sugestao(h1, ana, "Assistente de IA (exemplo)",
                "Avalie a história de adicionar ao carrinho.",
                "O sistema deve ser rápido e fácil de usar.");
        s2.rejeitar(ana, MotivoRejeicao.GENERICA, "Texto vago, sem exemplo concreto.");
        h1.getSugestoes().add(s1);
        h1.getSugestoes().add(s2);
        historiaRepository.save(h1);

        // História 2: com sugestão pendente (exportação bloqueada)
        HistoriaUsuario h2 = new HistoriaUsuario(loja, ana, "Finalizar compra",
                "cliente da loja", "pagar os itens do carrinho",
                "receber meu pedido em casa", Prioridade.MEDIA);
        h2.adicionarCenario(cenario("Pagamento aprovado", OrigemCenario.HUMANO,
                passo(TipoPasso.DADO, "que tenho itens no carrinho"),
                passo(TipoPasso.QUANDO, "eu pago com um cartão válido"),
                passo(TipoPasso.ENTAO, "o pedido é confirmado")));
        h2.getSugestoes().add(new Sugestao(h2, ana, "Assistente de IA (exemplo)",
                "Sugira cenários para finalizar compra.",
                "Cenário: cartão recusado. Quando o pagamento é recusado, então o pedido não é criado."));
        historiaRepository.save(h2);

        // História 3: do outro usuário, sem cenários (para testar bloqueio de autoria)
        historiaRepository.save(new HistoriaUsuario(loja, bruno, "Consultar histórico de pedidos",
                "cliente da loja", "ver meus pedidos anteriores",
                "acompanhar o que já comprei", Prioridade.BAIXA));
    }

    private static Passo passo(TipoPasso tipo, String texto) {
        return new Passo(tipo, texto);
    }

    private static Cenario cenario(String titulo, OrigemCenario origem, Passo... passos) {
        Cenario c = new Cenario(titulo, origem);
        c.substituirPassos(java.util.List.of(passos));
        return c;
    }
}
