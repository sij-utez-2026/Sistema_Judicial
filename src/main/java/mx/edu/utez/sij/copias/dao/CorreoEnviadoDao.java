package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.CorreoEnviado;

import java.util.List;

/** Correos enviados al solicitante (tabla cop_correo). Para MySQL: ver SolicitudCopiaDao. */
public interface CorreoEnviadoDao {

    /** SQL: INSERT INTO cop_correo (solicitud_id, tipo, destinatario, enviado_por, enviado_en, exito) VALUES (...) */
    CorreoEnviado save(CorreoEnviado correo);

    /** Los más recientes primero. SQL: {@code SELECT * FROM cop_correo WHERE solicitud_id = ? ORDER BY enviado_en DESC, id DESC} */
    List<CorreoEnviado> findBySolicitudIdOrderByEnviadoEnDesc(Long solicitudId);
}
