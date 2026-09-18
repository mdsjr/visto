package com.visto.visto.conhecimento;

import com.visto.visto.conhecimento.dto.ArtigoRequest;
import com.visto.visto.conhecimento.dto.ArtigoResponse;
import com.visto.visto.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/conhecimento")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Base de conhecimento", description = "Soluções documentadas e reutilizáveis")
public class ConhecimentoController {

    private final ConhecimentoService conhecimentoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    @Operation(summary = "Cria um artigo (problema, cenário, itens avaliados, procedimento)")
    public ResponseEntity<ArtigoResponse> criar(@RequestBody @Valid ArtigoRequest request,
                                                @AuthenticationPrincipal Usuario autor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conhecimentoService.criar(request, autor));
    }

    @GetMapping
    @Operation(summary = "Lista/busca artigos por termo (título, problema, cenário, tags)")
    public ResponseEntity<Page<ArtigoResponse>> listar(@RequestParam(required = false) String busca,
                                                       @RequestParam(defaultValue = "0") int pagina,
                                                       @RequestParam(defaultValue = "20") int tamanho) {
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), 100);
        return ResponseEntity.ok(conhecimentoService.listar(busca, PageRequest.of(Math.max(pagina, 0), tamanhoSeguro)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um artigo")
    public ResponseEntity<ArtigoResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(conhecimentoService.buscar(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    @Operation(summary = "Atualiza um artigo (autor ou admin)")
    public ResponseEntity<ArtigoResponse> atualizar(@PathVariable Long id,
                                                    @RequestBody @Valid ArtigoRequest request,
                                                    @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(conhecimentoService.atualizar(id, request, usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    @Operation(summary = "Exclui um artigo (autor ou admin)")
    public ResponseEntity<Void> excluir(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        conhecimentoService.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }
}
