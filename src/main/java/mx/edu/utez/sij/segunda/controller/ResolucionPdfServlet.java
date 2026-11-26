package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.dao.DocumentoSegDaoMySql;
import mx.edu.utez.sij.segunda.model.Resolucion;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Descarga en línea el último PDF de resolución guardado como BLOB en documento_Seg. */
@WebServlet("/segunda/resoluciones/pdf")
public class ResolucionPdfServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long id = Parametros.numero(request, "id");
        if (id == null) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        Optional<Resolucion> resolucion = Segunda.resolucionService().buscar(id);
        if (resolucion.isEmpty()) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
        Optional<Toca> toca = Segunda.tocaService().buscar(resolucion.get().getTocaId());
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        if (toca.isEmpty() || (SegundaPermisos.esMagistrado(usuario)
                && !usuario.getId().equals(toca.get().getMagistradoUsuarioId()))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Optional<DocumentoSegDaoMySql.Documento> documento = Segunda.documentoDao()
                .buscarUltimo(toca.get().getId(), "Resolución");
        if (documento.isEmpty() || documento.get().contenido() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String nombre = documento.get().titulo() == null ? "resolucion.pdf" : documento.get().titulo();
        nombre = nombre.replaceAll("[\\r\\n\\\"\\\\]", "_");
        String codificado = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + codificado);
        response.setContentLength(documento.get().contenido().length);
        response.getOutputStream().write(documento.get().contenido());
    }
}
