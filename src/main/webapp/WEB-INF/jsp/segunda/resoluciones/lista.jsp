<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<t:layout titulo="Resoluciones" modulo="segunda" seccion="resoluciones">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-resoluciones-lista">
    <n:migas>Resoluciones</n:migas>
    <n:encabezado titulo="Resoluciones"
                  descripcion="${segundaEsMagistrado
                      ? 'Resoluciones de sus tocas en segunda instancia.'
                      : 'Todas las resoluciones del sistema.'}"/>

    <form class="row g-2 my-3 sin-filtros" method="get" action="<c:url value='/segunda/resoluciones'/>">
        <input type="hidden" name="size" value="<c:out value='${size}'/>">
        <div class="col-lg-5 col-md-6">
            <div class="input-group">
                <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar"><n:icono nombre="buscar" tamano="14"/></button>
                <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
                       placeholder="Buscar por folio o expediente" aria-label="Buscar">
            </div>
        </div>
        <c:if test="${segundaEsAdmin}">
            <div class="col-lg-3 col-md-6">
                <select class="form-select" name="salaId" aria-label="Sala" data-autoenvio>
                    <option value="">Sala: Todas</option>
                    <c:forEach var="s" items="${salas}">
                        <option value="<c:out value='${s.id}'/>"${s.id eq salaId ? ' selected' : ''}>
                            <c:out value="${s.nombre}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </c:if>
    </form>

    <c:if test="${hayFiltros}">
        <div class="sij-texto-chico mb-3 sin-filtros-aplicados">
            Filtros aplicados · <c:out value="${pagina.totalElements}"/>
            <c:out value="${pagina.totalElements eq 1 ? 'resultado' : 'resultados'}"/>
            · <a href="<c:url value='/segunda/resoluciones'/>">Quitar filtros</a>
        </div>
    </c:if>

    <n:tarjeta sinRelleno="true" clase="sin-listado">
        <c:choose>
            <c:when test="${empty pagina.content}">
                <div class="p-4 text-center sij-descripcion">
                    No se encontraron resoluciones.
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table sij-tabla align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Toca / Expediente</th>
                            <th>Sentido</th>
                            <th>Fecha</th>
                            <th>Estado</th>
                            <th class="text-end">Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="r" items="${pagina.content}">
                            <tr>
                                <td>
                                    <div class="fw-semibold"><c:out value="${r.tocaFolio}"/></div>
                                    <div class="sij-texto-chico">Exp. <c:out value="${r.tocaExpediente}"/></div>
                                </td>
                                <td><n:estado valor="${r.sentido.etiqueta()}"/></td>
                                <td class="sij-texto-chico"><c:out value="${r.fechaTexto}"/></td>
                                <td><n:estado valor="${r.estado.etiqueta()}"/></td>
                                <td class="text-end">
                                    <a class="btn btn-sm btn-sij-secundario"
                                       href="<c:url value='/segunda/resoluciones/detalle?id=${r.id}'/>">Ver resolución</a>
                                    <a class="btn btn-sm btn-sij-secundario"
                                       href="<c:url value='/segunda/tocas/detalle?id=${r.tocaId}'/>">Ver toca</a>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>
    </n:tarjeta>

    <c:if test="${not empty pagina.content}">
        <div class="d-flex flex-wrap align-items-center gap-3 sin-paginacion">
            <form class="d-flex align-items-center gap-2" method="get" action="<c:url value='/segunda/resoluciones'/>">
                <input type="hidden" name="q" value="<c:out value='${q}'/>">
                <input type="hidden" name="salaId" value="<c:out value='${salaId}'/>">
                <label for="size" class="sij-texto-chico text-nowrap">Registros por página</label>
                <select id="size" name="size" class="form-select form-select-sm w-auto" data-autoenvio>
                    <c:forEach var="tam" items="${tamanosPagina}">
                        <option value="<c:out value='${tam}'/>"${tam eq size ? ' selected' : ''}><c:out value="${tam}"/></option>
                    </c:forEach>
                </select>
            </form>
            <div class="flex-grow-1">
                <n:paginacion pagina="${pagina}" url="/segunda/resoluciones"/>
            </div>
        </div>
    </c:if>

        </div>
    </jsp:body>
</t:layout>
