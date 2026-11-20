package mx.edu.utez.sij.copias.dao;

import mx.edu.utez.sij.copias.model.Pago;

import java.util.Optional;

/** TEMPORAL: ver DaoMemoria. */
public class PagoDaoMemoria extends DaoMemoria<Pago> implements PagoDao {

    @Override
    protected Long idDe(Pago registro) {
        return registro.getId();
    }

    @Override
    protected void asignarId(Pago registro, Long id) {
        registro.setId(id);
    }

    @Override
    public Optional<Pago> findBySolicitudId(Long solicitudId) {
        return filtrar(p -> p.getSolicitudId().equals(solicitudId)).stream().findFirst();
    }

    @Override
    public Optional<Pago> findByFolioPago(String folioPago) {
        return filtrar(p -> p.getFolioPago().equalsIgnoreCase(folioPago)).stream().findFirst();
    }

    @Override
    public boolean existsByFolioPago(String folioPago) {
        return !filtrar(p -> p.getFolioPago().equalsIgnoreCase(folioPago)).isEmpty();
    }
}
