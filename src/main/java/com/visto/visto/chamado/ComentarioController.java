package com.visto.visto.chamado;

import com.visto.visto.chamado.dto.ComentarioRequest;
import com.visto.visto.chamado.dto.ComentarioResponse;
import com.visto.visto.domain.usuario.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chamados/{chamadoId}/comentarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Comentários", description = "Comunicação entre solicitante e técnico dentro do chamado")
public class ComentarioController {

    private final ComentarioService comentarioService;

    @PostMapping
    @Operation(summary = "Adiciona um comentário ao chamado (interno = nota só para técnicos)")
    public ResponseEntity<ComentarioResponse> comentar(@PathVariable Long chamadoId,
                                                       @RequestBody @Valid ComentarioRequest request,
                                                       @AuthenticationPrincipal Usuario autor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comentarioService.comentar(chamadoId, request, autor));
    }

    @GetMapping
    @Operation(summary = "Lista os comentários do chamado (usuário comum não vê notas internas)")
    public ResponseEntity<List<ComentarioResponse>> listar(@PathVariable Long chamadoId,
                                                           @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(comentarioService.listar(chamadoId, usuario));
    }
}
