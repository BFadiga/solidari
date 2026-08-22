package br.com.fiap.solidari.backend.dto;

import br.com.fiap.solidari.backend.model.Papel;
import br.com.fiap.solidari.backend.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Double saldo,
        Papel papel
) {
    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getSaldo(),
                usuario.getPapel()
        );
    }
}
