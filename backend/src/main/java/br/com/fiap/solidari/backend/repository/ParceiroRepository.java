package br.com.fiap.solidari.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.solidari.backend.model.Parceiro;

public interface ParceiroRepository extends JpaRepository<Parceiro, Long> {

    List<Parceiro> findByCategoriaIgnoreCase(String categoria);
}
