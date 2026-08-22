package br.com.fiap.solidari.backend.dto;

public record AuthResponse(
        String token,
        String tipo,
        UsuarioResponse usuario
) {
    public AuthResponse(String token, UsuarioResponse usuario) {
        this(token, "Bearer", usuario);
    }
}
