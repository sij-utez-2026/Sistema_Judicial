<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  RF 5.2 Bitácora de auditoría (solo consulta). Boceto "Auditoría · Historial inmutable" (docs/modulos/Nathaly/).
  Los registros vienen de bitacora_Per (BitacoraDao).
--%>
<t:layout titulo="Bitácora de auditoría" modulo="peritajes" seccion="bitacora">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:body>
        <n:migas>Bitácora de auditoría</n:migas>

        <n:encabezado titulo="Bitácora de auditoría"
                      descripcion="Trazabilidad institucional de eventos. Los registros son permanentes y de solo consulta.">
            <span class="sij-contador">&bull; Solo lectura</span>
        </n:encabezado>

        <%-- Filtros: rango de fechas, usuario, tipo de evento y referencia (RF 5.2) --%>
        <form class="row g-2 my-3" method="get" action="<c:url value='/peritajes/bitacora'/>">
            <div class="col-6 col-lg-2">
                <label for="desde" class="form-label sij-texto-chico mb-1">Desde</label>
                <input type="date" id="desde" name="desde" class="form-control" value="<c:out value='${desde}'/>">
            </div>
            <div class="col-6 col-lg-2">
                <label for="hasta" class="form-label sij-texto-chico mb-1">Hasta</label>
                <input type="date" id="hasta" name="hasta" class="form-control" value="<c:out value='${hasta}'/>">
            </div>
            <div class="col-md-6 col-lg-4">
                <label for="usuario" class="form-label sij-texto-chico mb-1">Usuario</label>
                <select id="usuario" name="usuario" class="form-select" data-autoenvio>
                    <option value="">Todos los usuarios</option>
                    <c:forEach var="u" items="${usuarios}">
                        <option value="<c:out value='${u}'/>"${u eq usuario ? ' selected' : ''}><c:out value="${u}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-md-6 col-lg-4">
                <label for="evento" class="form-label sij-texto-chico mb-1">Evento</label>
                <select id="evento" name="evento" class="form-select" data-autoenvio>
                    <option value="">Todos los eventos</option>
                    <c:forEach var="e" items="${eventos}">
                        <option value="<c:out value='${e}'/>"${e eq evento ? ' selected' : ''}><c:out value="${e}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-6">
                <label for="q" class="form-label sij-texto-chico mb-1">Referencia</label>
                <div class="input-group">
                    <input type="search" id="q" name="q" class="form-control" value="<c:out value='${q}'/>"
                           placeholder="Folio o número de registro">
                    <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar"><n:icono nombre="buscar" tamano="14"/></button>
                </div>
            </div>
        </form>

        <c:if test="${hayFiltros}">
            <div class="sij-texto-chico mb-3">
                Filtros aplicados · <a href="<c:url value='/peritajes/bitacora'/>">Quitar filtros</a>
            </div>
        </c:if>

        <n:tarjeta sinRelleno="true">
            <c:choose>
                <c:when test="${empty pagina.content}">
                    <div class="p-4 text-center sij-descripcion">No hay movimientos con esos filtros</div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table sij-tabla align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Fecha / hora</th>
                                <th>Usuario</th>
                                <th>Referencia</th>
                                <th>Evento</th>
                                <th>Justificación / detalle</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="r" items="${pagina.content}">
                                <tr>
                                    <td class="text-nowrap">
                                        <div class="fw-semibold"><c:out value="${r.fechaTexto}"/></div>
                                        <div class="sij-texto-chico"><c:out value="${r.horaTexto}"/></div>
                                    </td>
                                    <td>
                                        <div><c:out value="${r.usuario}"/></div>
                                        <div class="sij-texto-chico"><c:out value="${r.rol}"/></div>
                                    </td>
                                    <td class="text-nowrap"><c:out value="${r.referencia}"/></td>
                                    <td><c:out value="${r.evento}"/></td>
                                    <td class="per-detalle"><c:out value="${r.detalle}"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </n:tarjeta>

        <div class="d-flex flex-wrap justify-content-between gap-2 sij-texto-chico mb-2">
            <span>Orden cronológico descendente</span>
            <span>Hora local de Morelos</span>
        </div>
        <n:paginacion pagina="${pagina}" url="/peritajes/bitacora"/>
    </jsp:body>
</t:layout>
