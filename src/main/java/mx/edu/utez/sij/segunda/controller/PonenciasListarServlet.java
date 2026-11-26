package mx.edu.utez.sij.segunda.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mx.edu.utez.sij.nucleo.web.Pagina;
import mx.edu.utez.sij.nucleo.web.Parametros;
import mx.edu.utez.sij.nucleo.web.Vistas;
import mx.edu.utez.sij.segunda.Segunda;
import mx.edu.utez.sij.segunda.dto.PonenciaListaItem;
import mx.edu.utez.sij.segunda.model.EstadoPonencia;
import mx.edu.utez.sij.segunda.model.Ponencia;
import mx.edu.utez.sij.segunda.model.Toca;
import mx.edu.utez.sij.nucleo.acceso.UsuarioSesion;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Lista las ponencias registradas, usando el DAO/servicio disponible. */
@WebServlet("/segunda/ponencias")
public class PonenciasListarServlet extends HttpServlet {

    private static final int TAMANO_POR_DEFECTO = 25;
    private static final List<Integer> TAMANOS_PAGINA = List.of(10, 25, 50, 100);
    private static final LocalDateTime DESDE = LocalDateTime.of(1000, 1, 1, 0, 0);
    private static final LocalDateTime HASTA = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioSesion usuario = Segunda.usuarioActual(request);
        String q = Parametros.texto(request, "q");
        String estadoTexto = Parametros.texto(request, "estado");
        int pagina = Math.max(0, Parametros.entero(request, "page", 0));
        int size = Parametros.entero(request, "size", TAMANO_POR_DEFECTO);
        if (!TAMANOS_PAGINA.contains(size)) size = TAMANO_POR_DEFECTO;

        EstadoPonencia estado = null;
        if (estadoTexto != null && !estadoTexto.isBlank()) {
            try {
                estado = EstadoPonencia.valueOf(estadoTexto);
            } catch (IllegalArgumentException ignored) {
                estadoTexto = "";
            }
        }

        String consulta = q == null ? "" : q.toLowerCase(Locale.ROOT).trim();
        List<PonenciaListaItem> filtradas = new ArrayList<>();
        for (Ponencia ponencia : Segunda.ponenciaService().entre(DESDE, HASTA)) {
            Toca toca = Segunda.tocaService().buscar(ponencia.getTocaId()).orElse(null);
            if (toca == null) continue;
            if (SegundaPermisos.esMagistrado(usuario)
                    && !usuario.getId().equals(toca.getMagistradoUsuarioId())) continue;
            if (estado != null && ponencia.getEstado() != estado) continue;

            PonenciaListaItem item = new PonenciaListaItem(
                    ponencia, toca.getFolioToca(), toca.getNumeroExpediente());
            if (!consulta.isEmpty()
                    && !contiene(item.getFolioToca(), consulta)
                    && !contiene(item.getNumeroExpediente(), consulta)) continue;
            filtradas.add(item);
        }

        Pagina<PonenciaListaItem> resultados = Pagina.de(filtradas, pagina, size);
        request.setAttribute("usuario", usuario);
        request.setAttribute("pagina", resultados);
        request.setAttribute("estados", EstadoPonencia.values());
        request.setAttribute("tamanosPagina", TAMANOS_PAGINA);
        request.setAttribute("q", q);
        request.setAttribute("estado", estadoTexto);
        request.setAttribute("size", resultados.getSize());
        request.setAttribute("hayFiltros", (q != null && !q.isBlank()) || estado != null);
        Vistas.mostrar(request, response, "segunda/ponencias/lista");
    }

    private boolean contiene(String valor, String consulta) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(consulta);
    }
}
