<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%--
  RF 4.1 Mis peritajes (Perito y Perito Administrador). Bocetos "Perito Administrador · Mis Peritajes" y
  "Perito Judicial · Mi espacio" (docs/modulos/Nathaly/).
  Sin "Fecha límite" (el DFR dice que no hay) y sin el panel de notificaciones (es del núcleo: /notificaciones).
--%>
<t:layout titulo="Mis peritajes" modulo="peritajes" seccion="mis-casos">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:body>
        <n:migas>Mis peritajes</n:migas>

        <n:encabezado titulo="Mis peritajes"
                      descripcion="Trabajo técnico personal · Solo se muestran los expedientes asignados a su cuenta."/>

        <div class="row g-3 my-1">
            <div class="col-sm-6">
                <div class="sij-tarjeta p-4 h-100">
                    <div class="sij-texto-chico">Mis peritajes Asignados</div>
                    <div class="per-numero"><c:out value="${asignados}"/></div>
                    <div class="sij-texto-chico">Pendientes de abrir expediente</div>
                </div>
            </div>
            <div class="col-sm-6">
                <div class="sij-tarjeta p-4 h-100">
                    <div class="sij-texto-chico">Mis peritajes En Proceso</div>
                    <div class="per-numero"><c:out value="${enProceso}"/></div>
                    <div class="sij-texto-chico">Con dictamen en preparación</div>
                </div>
            </div>
        </div>

        <form class="row g-2 my-2" method="get" action="<c:url value='/peritajes/mis-casos'/>">
            <div class="col-md-8">
                <div class="input-group">
                    <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar"><n:icono nombre="buscar" tamano="14"/></button>
                    <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
                           placeholder="Folio, demanda o expediente" aria-label="Buscar en mis peritajes por folio, demanda o expediente">
                </div>
            </div>
            <div class="col-md-4">
                <select class="form-select" name="estado" aria-label="Estado" data-autoenvio>
                    <option value="activos"${estado eq 'activos' ? ' selected' : ''}>Estado: Activos</option>
                    <option value="ASIGNADO"${estado eq 'ASIGNADO' ? ' selected' : ''}>Asignado</option>
                    <option value="EN_PROCESO"${estado eq 'EN_PROCESO' ? ' selected' : ''}>En Proceso</option>
                    <option value="DICTAMEN_ENTREGADO"${estado eq 'DICTAMEN_ENTREGADO' ? ' selected' : ''}>Dictamen Entregado</option>
                    <option value="todos"${estado eq 'todos' ? ' selected' : ''}>Todos</option>
                </select>
            </div>
        </form>

        <c:choose>
            <c:when test="${asignados + enProceso + entregados eq 0}">
                <%-- Texto del DFR 4.1 --%>
                <n:tarjeta><p class="text-center sij-descripcion mb-0">No tienes peritajes asignados</p></n:tarjeta>
            </c:when>
            <c:when test="${empty pagina.content}">
                <n:tarjeta><p class="text-center sij-descripcion mb-0">No se encontraron peritajes con esos filtros</p></n:tarjeta>
            </c:when>
            <c:otherwise>
                <p class="sij-texto-chico mb-2">
                    <c:out value="${pagina.totalElements}"/> <c:out value="${pagina.totalElements eq 1 ? 'peritaje' : 'peritajes'}"/>
                    · Ordenados por fecha de solicitud, del más reciente al más antiguo
                </p>
                <c:forEach var="caso" items="${pagina.content}">
                    <n:tarjeta>
                        <div class="d-flex flex-wrap justify-content-between align-items-start gap-2">
                            <h2 class="sij-tarjeta-titulo mb-1">Peritaje <c:out value="${caso.folio}"/></h2>
                            <n:estado valor="${caso.estado.texto}"/>
                        </div>
                        <p class="sij-descripcion mb-3">
                            <c:out value="${caso.referenciaDemanda}"/><c:if test="${not empty caso.juzgado}"> · <c:out value="${caso.juzgado}"/></c:if>
                        </p>
                        <dl class="row per-datos mb-2">
                            <div class="col-sm-4"><dt>Materia</dt><dd><c:out value="${caso.materia}"/></dd></div>
                            <div class="col-sm-4"><dt>Solicitud</dt><dd><c:out value="${caso.fechaSolicitudTexto}"/></dd></div>
                            <div class="col-sm-4"><dt>Especialidad</dt><dd><c:out value="${caso.especialidadesPerito}"/></dd></div>
                        </dl>
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
                            <span class="sij-texto-chico">
                                Asignado el <c:out value="${caso.fechaAsignacionTexto}"/>
                                <c:if test="${caso.estado eq 'ASIGNADO'}"> · Al abrir, el estado cambia a En Proceso y se registra la apertura.</c:if>
                            </span>
                            <c:choose>
                                <c:when test="${caso.estado eq 'EN_PROCESO'}">
                                    <a class="btn btn-primary" href="<c:url value='/peritajes/mis-casos/detalle?id=${caso.id}'/>">&rarr; Continuar dictamen</a>
                                </c:when>
                                <c:when test="${caso.estado eq 'ASIGNADO'}">
                                    <a class="btn btn-sij-secundario" href="<c:url value='/peritajes/mis-casos/detalle?id=${caso.id}'/>">&rarr; Abrir expediente</a>
                                </c:when>
                                <c:when test="${caso.estado eq 'DICTAMEN_ENTREGADO'}">
                                    <a class="btn btn-sij-secundario" href="<c:url value='/peritajes/mis-casos/detalle?id=${caso.id}'/>">Ver dictamen entregado</a>
                                </c:when>
                            </c:choose>
                        </div>
                    </n:tarjeta>
                </c:forEach>
                <n:paginacion pagina="${pagina}" url="/peritajes/mis-casos"/>
            </c:otherwise>
        </c:choose>
    </jsp:body>
</t:layout>
