package br.com.fiap.solidari.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.fiap.solidari.backend.model.Alerta;
import br.com.fiap.solidari.backend.repository.AlertaRepository;
import br.com.fiap.solidari.backend.repository.jdbc.ProcedureRepository;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final ProcedureRepository procedures;

    public AlertaService(AlertaRepository alertaRepository, ProcedureRepository procedures) {
        this.alertaRepository = alertaRepository;
        this.procedures = procedures;
    }

    public List<Alerta> pendentes() {
        return alertaRepository.findByResolvidoFalseOrderByCriadoEmDesc();
    }

    public int gerar() {
        return procedures.gerarAlertas();
    }
}
