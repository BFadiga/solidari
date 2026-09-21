package br.com.fiap.solidari.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.solidari.backend.model.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByResolvidoFalseOrderByCriadoEmDesc();
}
