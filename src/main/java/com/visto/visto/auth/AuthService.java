package com.visto.visto.auth;

import com.visto.visto.auth.dto.LoginRequest;
import com.visto.visto.auth.dto.RegistroRequest;
import com.visto.visto.auth.dto.TokenResponse;
import com.visto.visto.auth.dto.UsuarioResponse;
import com.visto.visto.domain.usuario.PerfilUsuario;
import com.visto.visto.domain.usuario.Usuario;
import com.visto.visto.exception.RegraNegocioException;
import com.visto.visto.repository.UsuarioRepository;
import com.visto.visto.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public TokenResponse registrar(RegistroRequest request) {
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com o e-mail " + email);
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome().trim())
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil() != null ? request.perfil() : PerfilUsuario.USUARIO)
                .build();

        usuario = usuarioRepository.save(usuario);
        return gerarResposta(usuario);
    }

    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        // Lança BadCredentialsException (tratada no GlobalExceptionHandler) se e-mail/senha estiverem errados
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.senha()));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));
        return gerarResposta(usuario);
    }

    private TokenResponse gerarResposta(Usuario usuario) {
        String token = jwtService.gerarToken(usuario);
        return TokenResponse.bearer(token, jwtService.getExpiracaoMillis(), UsuarioResponse.de(usuario));
    }
}
