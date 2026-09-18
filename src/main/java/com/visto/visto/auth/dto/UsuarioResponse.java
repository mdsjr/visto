package com.visto.visto.auth.dto;

import com.visto.visto.domain.usuario.PerfilUsuario;
import com.visto.visto.domain.usuario.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        LocalDateTime dataCriacao
) {
    public static UsuarioResponse de(Usuario u) {
        if (u == null) {
            return null;
        }
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(), u.getDataCriacao());
    }
}
