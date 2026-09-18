package com.visto.visto.conhecimento;

import com.visto.visto.conhecimento.dto.ArtigoRequest;
import com.visto.visto.conhecimento.dto.ArtigoResponse;
import com.visto.visto.domain.chamado.Chamado;
import com.visto.visto.domain.conhecimento.ArtigoConhecimento;
import com.visto.visto.domain.usuario.Usuario;
import com.visto.visto.exception.RecursoNaoEncontradoException;
import com.visto.visto.repository.ArtigoConhecimentoRepository;
import com.visto.visto.repository.ChamadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConhecimentoService {

    private final ArtigoConhecimentoRepository artigoRepository;
    private final ChamadoRepository chamadoRepository;

    @Transactional
    public ArtigoResponse criar(ArtigoRequest request, Usuario autor) {
        ArtigoConhecimento artigo = ArtigoConhecimento.builder()
                .autor(autor)
                .build();
        aplicar(artigo, request);
        return ArtigoResponse.de(artigoRepository.save(artigo));
    }

    @Transactional
    public ArtigoResponse atualizar(Long id, ArtigoRequest request, Usuario usuario) {
        ArtigoConhecimento artigo = obter(id);
        boolean autor = artigo.getAutor() != null && artigo.getAutor().getId().equals(usuario.getId());
        if (!autor && !usuario.isAdmin()) {
            throw new AccessDeniedException("Somente o autor ou um admin pode editar o artigo");
        }
        aplicar(artigo, request);
        return ArtigoResponse.de(artigoRepository.save(artigo));
    }

    @Transactional(readOnly = true)
    public ArtigoResponse buscar(Long id) {
        return ArtigoResponse.de(obter(id));
    }

    @Transactional(readOnly = true)
    public Page<ArtigoResponse> listar(String busca, Pageable pageable) {
        Page<ArtigoConhecimento> pagina;
        if (busca == null || busca.isBlank()) {
            pagina = artigoRepository.findAllByOrderByDataAtualizacaoDesc(pageable);
        } else {
            String termo = "%" + busca.trim().toLowerCase() + "%";
            pagina = artigoRepository.buscar(termo, pageable);
        }
        return pagina.map(ArtigoResponse::de);
    }

    @Transactional
    public void excluir(Long id, Usuario usuario) {
        ArtigoConhecimento artigo = obter(id);
        boolean autor = artigo.getAutor() != null && artigo.getAutor().getId().equals(usuario.getId());
        if (!autor && !usuario.isAdmin()) {
            throw new AccessDeniedException("Somente o autor ou um admin pode excluir o artigo");
        }
        artigoRepository.delete(artigo);
    }

    // ---- helpers ----

    private void aplicar(ArtigoConhecimento artigo, ArtigoRequest request) {
        artigo.setTitulo(request.titulo().trim());
        artigo.setProblema(request.problema().trim());
        artigo.setCenario(limpar(request.cenario()));
        artigo.setItensAvaliados(limpar(request.itensAvaliados()));
        artigo.setProcedimento(request.procedimento().trim());
        artigo.setTags(limpar(request.tags()));

        if (request.chamadoOrigemId() != null) {
            Chamado chamado = chamadoRepository.findById(request.chamadoOrigemId())
                    .orElseThrow(() -> RecursoNaoEncontradoException.de("Chamado", request.chamadoOrigemId()));
            artigo.setChamadoOrigem(chamado);
        } else {
            artigo.setChamadoOrigem(null);
        }
    }

    private static String limpar(String valor) {
        if (valor == null) {
            return null;
        }
        String v = valor.trim();
        return v.isEmpty() ? null : v;
    }

    private ArtigoConhecimento obter(Long id) {
        return artigoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.de("Artigo", id));
    }
}
