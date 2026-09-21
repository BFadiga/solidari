package br.com.fiap.solidari.backend.service;

import java.math.BigDecimal;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.fiap.solidari.backend.dto.AuthResponse;
import br.com.fiap.solidari.backend.dto.LoginRequest;
import br.com.fiap.solidari.backend.dto.RegistroRequest;
import br.com.fiap.solidari.backend.dto.UsuarioResponse;
import br.com.fiap.solidari.backend.exception.RegraNegocioException;
import br.com.fiap.solidari.backend.model.Papel;
import br.com.fiap.solidari.backend.model.Usuario;
import br.com.fiap.solidari.backend.repository.UsuarioRepository;
import br.com.fiap.solidari.backend.security.JwtService;
import br.com.fiap.solidari.backend.security.UsuarioDetailsService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UsuarioDetailsService usuarioDetailsService,
            AuthenticationManager authenticationManager
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordEncoder.encode(request.senha()));
        usuario.setPapel(Papel.USUARIO);
        usuario.setSaldo(BigDecimal.ZERO);

        usuario = usuarioRepository.save(usuario);

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.gerarToken(userDetails);

        return new AuthResponse(token, UsuarioResponse.de(usuario));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado"));

        UserDetails userDetails = usuarioDetailsService.loadUserByUsername(usuario.getEmail());
        String token = jwtService.gerarToken(userDetails);

        return new AuthResponse(token, UsuarioResponse.de(usuario));
    }
}
