package com.visto.visto.chamado;

import com.visto.visto.chamado.dto.AcompanhamentoPublicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Link público de acompanhamento: o solicitante recebe o código e consulta o status sem login.
 */
@RestController
@RequestMapping("/public/chamados")
@RequiredArgsConstructor
@Tag(name = "Acompanhamento público", description = "Consulta de status sem autenticação")
public class AcompanhamentoPublicoController {

    private final ChamadoService chamadoService;

    @GetMapping("/{codigoPublico}")
    @Operation(summary = "Consulta o status de um chamado pelo código público")
    public ResponseEntity<AcompanhamentoPublicoResponse> acompanhar(@PathVariable String codigoPublico) {
        return ResponseEntity.ok(chamadoService.acompanhar(codigoPublico));
    }
}
