package com.visto.visto.chamado;

import com.visto.visto.chamado.dto.AbrirChamadoRequest;
import com.visto.visto.chamado.dto.AlterarPrioridadeRequest;
import com.visto.visto.chamado.dto.AlterarStatusRequest;
import com.visto.visto.chamado.dto.ChamadoResponse;
import com.visto.visto.domain.chamado.PrioridadeChamado;
import com.visto.visto.domain.chamado.StatusChamado;
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
@RequestMapping("/chamados")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Chamados", description = "Abertura, fila e atendimento de chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;

    @PostMapping
    @Operation(summary = "Abre um novo chamado em nome do usuário autenticado")
    public ResponseEntity<ChamadoResponse> abrir(@RequestBody @Valid AbrirChamadoRequest request,
                                                 @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chamadoService.abrir(request, usuario));
    }

    @GetMapping
    @Operation(summary = "Lista chamados: fila completa para técnico/admin, só os próprios para usuário comum")
    public ResponseEntity<Page<ChamadoResponse>> listar(@RequestParam(required = false) StatusChamado status,
                                                        @RequestParam(required = false) PrioridadeChamado prioridade,
                                                        @RequestParam(defaultValue = "0") int pagina,
                                                        @RequestParam(defaultValue = "20") int tamanho,
                                                        @AuthenticationPrincipal Usuario usuario) {
        int tamanhoSeguro = Math.min(Math.max(tamanho, 1), 100);
        return ResponseEntity.ok(chamadoService.listar(usuario, status, prioridade,
                PageRequest.of(Math.max(pagina, 0), tamanhoSeguro)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um chamado")
    public ResponseEntity<ChamadoResponse> buscar(@PathVariable Long id,
                                                  @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(chamadoService.buscar(id, usuario));
    }

    @DeleteMapping("/{id}/visualizacao")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    @Operation(summary = "Técnico saiu da tela do chamado (encerra o aviso de atendimento simultâneo)")
    public ResponseEntity<Void> sair(@PathVariable Long id, @AuthenticationPrincipal Usuario tecnico) {
        chamadoService.sairDoChamado(id, tecnico);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/assumir")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    @Operation(summary = "Técnico assume o chamado (passa para EM_ATENDIMENTO)")
    public ResponseEntity<ChamadoResponse> assumir(@PathVariable Long id,
                                                   @AuthenticationPrincipal Usuario tecnico) {
        return ResponseEntity.ok(chamadoService.assumir(id, tecnico));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status (técnico/admin; solicitante só pode FECHAR o próprio chamado)")
    public ResponseEntity<ChamadoResponse> alterarStatus(@PathVariable Long id,
                                                         @RequestBody @Valid AlterarStatusRequest request,
                                                         @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(chamadoService.alterarStatus(id, request.status(), usuario));
    }

    @PatchMapping("/{id}/prioridade")
    @PreAuthorize("hasAnyRole('TECNICO', 'ADMIN')")
    @Operation(summary = "Altera a prioridade e recalcula o prazo de SLA")
    public ResponseEntity<ChamadoResponse> alterarPrioridade(@PathVariable Long id,
                                                             @RequestBody @Valid AlterarPrioridadeRequest request) {
        return ResponseEntity.ok(chamadoService.alterarPrioridade(id, request.prioridade()));
    }
}
