<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  Solicitudes y peritajes (administradores). Boceto "Peritajes · Listado" (docs/modulos/Nathaly/).
  Sin "Fecha límite" ni filtro por mes: el DFR dice que el dictamen no tiene fecha límite (decisión del equipo).
--%>
<t:layout titulo="Solicitudes y peritajes" modulo="peritajes" seccion="casos">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:body>
        <n:migas>Solicitudes y peritajes</n:migas>

        <n:encabezado titulo="Peritajes" descripcion="Consulte el expediente, la asignación y el estado de cada solicitud.">
            <span class="sij-contador">&bull; <c:out value="${total}"/> peritajes registrados</span>
        </n:encabezado>

        <%-- Pestañas rápidas por estado --%>
        <nav class="per-pestanas my-3" aria-label="Filtrar por estado">
            <a class="${empty estado ? 'activa' : ''}" href="<c:url value='/peritajes/casos'/>">Todos (<c:out value="${total}"/>)</a>
            <%-- Sin perito de la materia: se vuelven a intentar asignar cada vez que se abre esta lista --%>
            <a class="${estado eq 'PENDIENTE_ASIGNACION' ? 'activa' : ''}" href="<c:url value='/peritajes/casos?estado=PENDIENTE_ASIGNACION'/>">Pendientes (<c:out value="${porEstado['PENDIENTE_ASIGNACION']}"/>)</a>
            <a class="${estado eq 'ASIGNADO' ? 'activa' : ''}" href="<c:url value='/peritajes/casos?estado=ASIGNADO'/>">Asignados (<c:out value="${porEstado['ASIGNADO']}"/>)</a>
            <a class="${estado eq 'EN_PROCESO' ? 'activa' : ''}" href="<c:url value='/peritajes/casos?estado=EN_PROCESO'/>">En Proceso (<c:out value="${porEstado['EN_PROCESO']}"/>)</a>
            <a class="${estado eq 'DICTAMEN_ENTREGADO' ? 'activa' : ''}" href="<c:url value='/peritajes/casos?estado=DICTAMEN_ENTREGADO'/>">Entregados (<c:out value="${porEstado['DICTAMEN_ENTREGADO']}"/>)</a>
        </nav>

        <%-- Filtros (peritajes.md 8: estado, materia, perito y folio). Las listas se aplican al cambiar. --%>
        <form class="row g-2 mb-2" method="get" action="<c:url value='/peritajes/casos'/>">
            <div class="col-lg-3 col-md-6">
                <div class="input-group">
                    <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar"><n:icono nombre="buscar" tamano="14"/></button>
                    <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
                           placeholder="Folio, demanda o expediente" aria-label="Buscar por folio, demanda o expediente">
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <select class="form-select" name="materia" aria-label="Materia" data-autoenvio>
                    <option value="">Todas las materias</option>
                    <c:forEach var="m" items="${materias}">
                        <option value="<c:out value='${m.clave}'/>"${m.clave eq materia ? ' selected' : ''}><c:out value="${m.nombre}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-3 col-md-6">
                <select class="form-select" name="estado" aria-label="Estado" data-autoenvio>
                    <option value="">Todos los estados</option>
                    <c:forEach var="e" items="${estados}">
                        <option value="<c:out value='${e}'/>"${e.name() eq estado ? ' selected' : ''}><c:out value="${e.texto}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-3 col-md-6">
                <select class="form-select" name="peritoId" aria-label="Perito" data-autoenvio>
                    <option value="">Todos los peritos</option>
                    <c:forEach var="per" items="${peritos}">
                        <option value="<c:out value='${per.id}'/>"${per.id eq peritoId ? ' selected' : ''}><c:out value="${per.nombreCompleto}"/></option>
                    </c:forEach>
                </select>
            </div>
        </form>

        <c:if test="${hayFiltros}">
            <div class="sij-texto-chico mb-3">
                Filtros aplicados · <c:out value="${pagina.totalElements}"/>
                <c:out value="${pagina.totalElements eq 1 ? 'resultado coincidente' : 'resultados coincidentes'}"/>
                · <a href="<c:url value='/peritajes/casos'/>">Quitar filtros</a>
            </div>
        </c:if>

        <n:tarjeta sinRelleno="true">
            <c:choose>
                <c:when test="${empty pagina.content}">
                    <div class="p-4 text-center sij-descripcion">
                        <c:out value="${total eq 0 ? 'Aún no hay solicitudes de peritaje.' : 'No se encontraron peritajes'}"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table sij-tabla align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Folio / Expediente</th>
                                <th>Materia</th>
                                <th>Perito asignado</th>
                                <th>Estado</th>
                                <th>Fecha de solicitud</th>
                                <th class="text-end">Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="caso" items="${pagina.content}">
                                <tr>
                                    <td>
                                        <div class="fw-semibold">Peritaje <c:out value="${caso.folio}"/></div>
                                        <div class="sij-texto-chico"><c:out value="${caso.referenciaDemanda}"/><c:if test="${not empty caso.juzgado}"> · <c:out value="${caso.juzgado}"/></c:if></div>
                                    </td>
                                    <td><c:out value="${caso.materia}"/></td>
                                    <td><c:out value="${caso.peritoNombreTexto}"/></td>
                                    <td><n:estado valor="${caso.estado.texto}"/></td>
                                    <td class="text-nowrap"><c:out value="${caso.fechaSolicitudTexto}"/></td>
                                    <td class="text-end">
                                        <a class="btn btn-sm btn-sij-secundario" href="<c:url value='/peritajes/casos/detalle?id=${caso.id}'/>"
                                           title="Ver detalle" aria-label="Ver detalle del peritaje <c:out value='${caso.folio}'/>">&#8599;</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </n:tarjeta>

        <n:paginacion pagina="${pagina}" url="/peritajes/casos"/>
    </jsp:body>
</t:layout>
