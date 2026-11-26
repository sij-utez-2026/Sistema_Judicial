package mx.edu.utez.sij.segunda.service;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.nucleo.contratos.segunda.RecepcionTocaApi;
import mx.edu.utez.sij.nucleo.contratos.segunda.SolicitudTocaDto;
import mx.edu.utez.sij.segunda.dao.RecepcionPendienteDaoMySql;
import mx.edu.utez.sij.segunda.dto.RecepcionPendienteDto;

import java.util.Objects;

/**
 * Adaptador del contrato compartido. Oficialía entrega el expediente y la sala;
 * Segunda consulta el resto de los datos y resuelve el magistrado desde la sala.
 */
public class RecepcionTocaService implements RecepcionTocaApi {

    private final TocaService tocaService;
    private final RecepcionPendienteDaoMySql recepcionPendienteDao;

    public RecepcionTocaService(TocaService tocaService) {
        this(tocaService, new RecepcionPendienteDaoMySql());
    }

    public RecepcionTocaService(TocaService tocaService,
                                RecepcionPendienteDaoMySql recepcionPendienteDao) {
        this.tocaService = tocaService;
        this.recepcionPendienteDao = recepcionPendienteDao;
    }

    @Override
    public String recibir(SolicitudTocaDto solicitud) {
        if (solicitud == null || solicitud.numeroExpediente() == null
                || solicitud.numeroExpediente().isBlank() || solicitud.salaId() == null) {
            throw new ReglaNegocioException("El número de expediente y la sala son obligatorios");
        }
        RecepcionPendienteDto pendiente = recepcionPendienteDao
                .buscarPendiente(solicitud.numeroExpediente().trim())
                .orElseThrow(() -> new ReglaNegocioException(
                        "El expediente no está pendiente de recepción en Segunda Instancia"));
        if (!Objects.equals(pendiente.getSalaId(), solicitud.salaId())) {
            throw new ReglaNegocioException(
                    "La sala recibida no coincide con la sala asignada por Oficialía");
        }
        return tocaService.recibirDeContratoOficialia(
                pendiente.getNumeroExpediente(), pendiente.getMateriaId(),
                pendiente.getJuzgadoOrigenId(), pendiente.getDistritoId(),
                pendiente.getSalaId()).getFolioToca();
    }
}
