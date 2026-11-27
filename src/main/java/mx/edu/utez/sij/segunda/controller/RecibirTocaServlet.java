package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.FlashMessage;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.dto.RecepcionPendienteDto;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.util.List;

/** Flujo de recepción de Segunda; solo permite expedientes enviados por Oficialía. */
@WebServlet("/segunda/tocas/recibir")
public class RecibirTocaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!esAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        cargarPendientes(request);
        Vistas.mostrar(request, response, "segunda/tocas/recibir");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!esAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.setCharacterEncoding("UTF-8");
        String numero = texto(request.getParameter("numeroExpediente"));
        request.setAttribute("expedienteSeleccionado", numero);

        try {
            if (numero == null) throw new IllegalArgumentException("Seleccione un expediente pendiente de recepción.");
            RecepcionPendienteDto origen = Segunda.recepcionPendienteDao().buscarPendiente(numero)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El expediente no está enviado a Segunda o ya fue recibido."));
            Toca toca = Segunda.tocaService().recibirDeContratoOficialia(
                    origen.getNumeroExpediente(), origen.getMateriaId(), origen.getJuzgadoOrigenId(),
                    origen.getDistritoId(), origen.getSalaId());
            UsuarioSesion usuario = Segunda.usuarioActual(request);
            Segunda.registrarBitacora(request, usuario, "toca_Seg", toca.getId(), "Recepción de toca",
                    "Expediente " + origen.getNumeroExpediente() + " · sala " + origen.getSalaNombre(), "green");
            FlashMessage.exito(request, "Recepción registrada",
                    "La toca " + toca.getFolioToca() + " quedó recibida y asignada al magistrado de la sala.");
            Vistas.redirigir(request, response, "/segunda/tocas");
            return;
        } catch (IllegalArgumentException | IllegalStateException e) {
            request.setAttribute("error", e.getMessage());
        }

        cargarPendientes(request);
        Vistas.mostrar(request, response, "segunda/tocas/recibir");
    }

    private void cargarPendientes(HttpServletRequest request) {
        try {
            List<RecepcionPendienteDto> pendientes = Segunda.recepcionPendienteDao().listar();
            request.setAttribute("pendientes", pendientes);
        } catch (RuntimeException e) {
            request.setAttribute("pendientes", List.of());
            request.setAttribute("errorCarga", "No fue posible consultar la base de datos. Verifique SIJ_BD_URL y las credenciales de desarrollo.");
        }
    }

    private boolean esAdmin(HttpServletRequest request) {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        return usuario != null && SegundaPermisos.esAdmin(usuario);
    }

    private String texto(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
