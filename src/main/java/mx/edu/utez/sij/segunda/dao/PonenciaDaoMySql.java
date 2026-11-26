package mx.edu.utez.sij.segunda.dao;

import mx.edu.utez.sij.nucleo.contratos.ReglaNegocioException;
import mx.edu.utez.sij.segunda.model.EstadoPonencia;
import mx.edu.utez.sij.segunda.model.Ponencia;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO MySQL para ponencia_Seg. */
public class PonenciaDaoMySql implements PonenciaDao {
    private static final String BASE = "SELECT p.id_ponencia,p.id_toca,p.id_magistrado,p.fecha_sesion,p.hora_inicio,p.hora_fin,p.tipo_sesion,p.fecha_creacion,t.numero_toca "
            + "FROM ponencia_Seg p JOIN toca_Seg t ON t.id_toca = p.id_toca ";

    @Override public Optional<Ponencia> findById(Long id) {
        if (id == null) return Optional.empty();
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement p = c.prepareStatement(BASE + "WHERE p.id_ponencia = ?")) {
            p.setLong(1, id); try (ResultSet r = p.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
        } catch (SQLException e) { throw DaoJdbcSupport.error("buscar ponencia", e); }
    }
    @Override public Ponencia save(Ponencia p) {
        String sql = p.getId() == null
                ? "INSERT INTO ponencia_Seg (id_toca,id_magistrado,fecha_sesion,hora_inicio,hora_fin,tipo_sesion) VALUES (?,?,?,?,?,?)"
                : "UPDATE ponencia_Seg SET id_toca=?,id_magistrado=?,fecha_sesion=?,hora_inicio=?,hora_fin=?,tipo_sesion=? WHERE id_ponencia=?";
        try (Connection c = DaoJdbcSupport.obtener(); PreparedStatement q = c.prepareStatement(sql, p.getId() == null ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS)) {
            q.setLong(1,p.getTocaId()); q.setLong(2,p.getMagistradoId()); q.setDate(3, Date.valueOf(p.getFecha())); q.setTime(4, Time.valueOf(p.getHora())); q.setTime(5, Time.valueOf(p.getHoraFin())); q.setString(6,null);
            if(p.getId()!=null) q.setLong(7,p.getId()); if(q.executeUpdate()!=1) throw new IllegalStateException("La ponencia no pudo guardarse");
            if(p.getId()==null) try(ResultSet k=q.getGeneratedKeys()){if(k.next())p.setId(k.getLong(1));}
            return findById(p.getId()).orElseThrow(() -> new IllegalStateException("No se pudo releer la ponencia"));
        } catch(SQLException e){throw DaoJdbcSupport.error("guardar ponencia",e);}
    }

    @Override public boolean registrarAtomico(Ponencia ponencia, boolean iniciarPonencia, String descripcionHistorial) {
        if (ponencia == null || ponencia.getTocaId() == null || ponencia.getMagistradoId() == null
                || ponencia.getFechaHora() == null) {
            throw new ReglaNegocioException("Los datos de la ponencia están incompletos");
        }
        try (Connection c = DaoJdbcSupport.obtener()) {
            c.setAutoCommit(false);
            try {
                String estadoToca;
                long magistradoToca;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT estado,id_magistrado FROM toca_Seg WHERE id_toca=? FOR UPDATE")) {
                    p.setLong(1, ponencia.getTocaId());
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new ReglaNegocioException("La toca ya no existe");
                        estadoToca = r.getString("estado");
                        magistradoToca = r.getLong("id_magistrado");
                        if (r.wasNull()) throw new ReglaNegocioException("La toca no tiene magistrado asignado");
                    }
                }
                if (!ponencia.getMagistradoId().equals(magistradoToca)) {
                    throw new ReglaNegocioException("El magistrado asignado a la toca cambió; vuelva a cargar la pantalla");
                }
                if ("Resuelto".equals(estadoToca)) {
                    throw new ReglaNegocioException("No se puede programar una ponencia en una toca resuelta");
                }
                if (iniciarPonencia && !"Nueva".equals(estadoToca)
                        || !iniciarPonencia && !"En ponencia".equals(estadoToca)) {
                    throw new ReglaNegocioException("El estado de la toca cambió; vuelva a cargar la pantalla");
                }

                try (PreparedStatement p = c.prepareStatement(
                        "SELECT m.id_magistrado FROM magistrado_Seg m JOIN user_Seg u ON u.id_usuario=m.id_usuario "
                                + "WHERE m.id_magistrado=? AND m.estatus=TRUE AND u.Status=TRUE FOR UPDATE")) {
                    p.setLong(1, ponencia.getMagistradoId());
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new ReglaNegocioException("El magistrado ya no está activo");
                    }
                }

                var inicio = ponencia.getFechaHora();
                var desde = inicio.minusHours(1);
                var hasta = inicio.plusHours(1);
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT id_ponencia FROM ponencia_Seg WHERE id_magistrado=? "
                                + "AND TIMESTAMP(fecha_sesion,hora_inicio) BETWEEN ? AND ? LIMIT 1")) {
                    p.setLong(1, ponencia.getMagistradoId());
                    p.setTimestamp(2, Timestamp.valueOf(desde));
                    p.setTimestamp(3, Timestamp.valueOf(hasta));
                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) throw new ReglaNegocioException("El magistrado ya tiene una ponencia en ese horario (±1 h)");
                    }
                }

                String insert = "INSERT INTO ponencia_Seg (id_toca,id_magistrado,fecha_sesion,hora_inicio,hora_fin,tipo_sesion) "
                        + "VALUES (?,?,?,?,?,?)";
                try (PreparedStatement p = c.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    p.setLong(1, ponencia.getTocaId());
                    p.setLong(2, ponencia.getMagistradoId());
                    p.setDate(3, Date.valueOf(ponencia.getFecha()));
                    p.setTime(4, Time.valueOf(ponencia.getHora()));
                    p.setTime(5, Time.valueOf(ponencia.getHoraFin()));
                    p.setString(6, null);
                    if (p.executeUpdate() != 1) throw new SQLException("No se registró la ponencia");
                    try (ResultSet k = p.getGeneratedKeys()) {
                        if (!k.next()) throw new SQLException("MySQL no devolvió el identificador de la ponencia");
                        ponencia.setId(k.getLong(1));
                    }
                }

                if (iniciarPonencia) {
                    try (PreparedStatement p = c.prepareStatement(
                            "UPDATE toca_Seg SET estado='En ponencia' WHERE id_toca=? AND estado='Nueva'")) {
                        p.setLong(1, ponencia.getTocaId());
                        if (p.executeUpdate() != 1) throw new SQLException("No se pudo iniciar el estado En ponencia");
                    }
                }
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO historial_toca_Seg (id_toca,descripcion) VALUES (?,?)")) {
                    p.setLong(1, ponencia.getTocaId());
                    p.setString(2, historial(descripcionHistorial));
                    if (p.executeUpdate() != 1) throw new SQLException("No se registró el historial de la ponencia");
                }
                c.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw DaoJdbcSupport.error("registrar ponencia y actualizar la toca", e);
        }
    }

    private static String historial(String descripcion) {
        String texto = descripcion == null || descripcion.isBlank() ? "Ponencia registrada" : descripcion.trim();
        return texto.length() <= 300 ? texto : texto.substring(0, 300);
    }
    @Override public List<Ponencia> findByTocaId(Long tocaId) {
        return listar(BASE+"WHERE p.id_toca = ? ORDER BY p.fecha_sesion,p.hora_inicio", p->p.setLong(1,tocaId));
    }
    @Override public List<Ponencia> findByMagistradoEntre(Long magistradoId, LocalDateTime desde, LocalDateTime hasta) {
        if(magistradoId==null)return List.of();
        String sql=BASE+"WHERE p.id_magistrado = ? AND TIMESTAMP(p.fecha_sesion,p.hora_inicio) BETWEEN ? AND ? ORDER BY p.fecha_sesion,p.hora_inicio";
        return listar(sql,p->{p.setLong(1,magistradoId);p.setTimestamp(2, Timestamp.valueOf(desde));p.setTimestamp(3, Timestamp.valueOf(hasta));});
    }
    @Override public boolean tienePonenciaHoy(Long tocaId) {
        if(tocaId==null)return false;
        try(Connection c=DaoJdbcSupport.obtener();PreparedStatement p=c.prepareStatement("SELECT 1 FROM ponencia_Seg WHERE id_toca=? AND fecha_sesion=CURRENT_DATE LIMIT 1")){
            p.setLong(1,tocaId);try(ResultSet r=p.executeQuery()){return r.next();}
        }catch(SQLException e){throw DaoJdbcSupport.error("consultar ponencia del día",e);}
    }
    @Override public List<Ponencia> entre(LocalDateTime desde, LocalDateTime hasta) {
        String sql=BASE+"WHERE TIMESTAMP(p.fecha_sesion,p.hora_inicio) BETWEEN ? AND ? ORDER BY p.fecha_sesion,p.hora_inicio";
        return listar(sql,p->{p.setTimestamp(1, Timestamp.valueOf(desde));p.setTimestamp(2, Timestamp.valueOf(hasta));});
    }
    private List<Ponencia> listar(String sql, Binder binder){
        List<Ponencia> out=new ArrayList<>();try(Connection c=DaoJdbcSupport.obtener();PreparedStatement p=c.prepareStatement(sql)){binder.bind(p);try(ResultSet r=p.executeQuery()){while(r.next())out.add(map(r));}return List.copyOf(out);}catch(SQLException e){throw DaoJdbcSupport.error("listar ponencias",e);}
    }
    private static Ponencia map(ResultSet r)throws SQLException{
        Ponencia p=new Ponencia();p.setId(r.getLong("id_ponencia"));p.setTocaId(r.getLong("id_toca"));p.setMagistradoId(r.getLong("id_magistrado"));
        var fecha=r.getDate("fecha_sesion");var hora=r.getTime("hora_inicio");if(fecha!=null&&hora!=null)p.setFechaHora(fecha.toLocalDate().atTime(hora.toLocalTime()));
        p.setEstado(EstadoPonencia.PROGRAMADA);p.setTocaFolio(r.getString("numero_toca"));return p;
    }
    @FunctionalInterface private interface Binder{void bind(PreparedStatement p)throws SQLException;}
}
