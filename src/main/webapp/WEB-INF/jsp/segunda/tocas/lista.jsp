<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- Listado de tocas con modal de detalle rápido --%>
<t:layout titulo="Mis tocas" modulo="segunda" seccion="tocas">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-tocas-lista">
    <n:migas>Tocas</n:migas>

    <n:encabezado
            titulo="${segundaEsMagistrado ? 'Mis tocas' : 'Tocas'}"
            descripcion="${segundaEsMagistrado
                ? 'Tocas asignadas a su sala en segunda instancia.'
                : 'Todas las tocas del sistema en segunda instancia.'}"/>

    <div class="d-flex flex-wrap gap-2 my-3 sin-resumen">
        <span class="sij-contador">&bull; <c:out value="${totalRegistradas}"/> Registradas</span>
        <span class="sij-contador">&bull; <c:out value="${totalAsignadas}"/> Nuevas</span>
        <span class="sij-contador">&bull; <c:out value="${totalEnPonencia}"/> En ponencia</span>
        <span class="sij-contador">&bull; <c:out value="${totalResueltas}"/> Resueltas</span>
    </div>
    <div class="d-flex justify-content-end gap-2 mb-3">
        <c:if test="${segundaEsAdmin}">
            <a class="btn btn-primary" href="<c:url value='/segunda/tocas/recibir'/>">Recibir expediente enviado</a>
            <a class="btn btn-sij-secundario" href="<c:url value='/segunda/tocas/reasignar'/>">Reasignar toca</a>
        </c:if>
    </div>

    <form class="row g-2 mb-2 sin-filtros" method="get" action="<c:url value='/segunda/tocas'/>">
        <input type="hidden" name="size" value="<c:out value='${size}'/>">
        <div class="col-lg-5 col-md-6">
            <div class="input-group">
                <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar">
                    <n:icono nombre="buscar" tamano="14"/>
                </button>
                <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
                       placeholder="Buscar por folio o expediente" aria-label="Buscar">
            </div>
        </div>
        <div class="col-lg-3 col-md-6">
            <select class="form-select" name="estado" aria-label="Estado" data-autoenvio>
                <option value="">Estado: Todos</option>
                <c:forEach var="e" items="${estados}">
                    <option value="<c:out value='${e.name()}'/>"${e.name() eq estado ? ' selected' : ''}>
                        <c:out value="${e.etiqueta()}"/>
                    </option>
                </c:forEach>
            </select>
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
            · <a href="<c:url value='/segunda/tocas'/>">Quitar filtros</a>
        </div>
    </c:if>

    <n:tarjeta sinRelleno="true" clase="sin-listado">
        <c:choose>
            <c:when test="${empty pagina.content}">
                <div class="p-4 text-center sij-descripcion">
                    <c:out value="${totalRegistradas eq 0
                        ? 'Aún no hay tocas registradas.'
                        : 'No se encontraron tocas'}"/>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table sij-tabla align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Toca / Expediente</th>
                            <th>Materia</th>
                            <th>Sala</th>
                            <th>Recepción</th>
                            <th>Estado</th>
                            <th class="text-end">Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="t" items="${pagina.content}">
                            <tr>
                                <td>
                                    <div class="fw-semibold"><c:out value="${t.folioToca}"/></div>
                                    <div class="sij-texto-chico">Exp. <c:out value="${t.numeroExpediente}"/></div>
                                </td>
                                <td><c:out value="${t.materiaNombre}"/></td>
                                <td><c:out value="${t.salaNombre}"/></td>
                                <td class="sij-texto-chico"><c:out value="${t.fechaRecepcionTexto}"/></td>
                                <td><n:estado valor="${t.estado.etiqueta()}"/></td>
                                <td class="text-end">
                                    <div class="btn-group" role="group">
                                            <%-- Ver detalle en modal --%>
                                        <button type="button" class="btn btn-sm btn-sij-secundario"
                                                data-bs-toggle="modal" data-bs-target="#modalDetalle${t.id}"
                                                title="Ver detalle">
                                            <n:icono nombre="documento" tamano="14"/>
                                        </button>
                                            <%-- Ir a la página completa de la toca --%>
                                        <a class="btn btn-sm btn-sij-secundario"
                                           href="<c:url value='/segunda/tocas/detalle?id=${t.id}'/>"
                                           title="Abrir toca completa">
                                            <n:icono nombre="carpeta" tamano="14"/>
                                        </a>
                                    </div>
                                </td>
                            </tr>

                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            
                    <c:forEach var="t" items="${pagina.content}">
<%-- ============ MODAL DETALLE RÁPIDO ============ --%>
                            <div class="modal fade" id="modalDetalle${t.id}" tabindex="-1"
                                 aria-labelledby="tituloDetalle${t.id}" aria-hidden="true">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h5 class="modal-title" id="tituloDetalle${t.id}">
                                                Toca <c:out value="${t.folioToca}"/>
                                            </h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"
                                                    aria-label="Cerrar"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="row g-3 sij-texto-chico sin-datos">
                                                <div class="col-md-6">
                                                    <div class="text-muted">Folio</div>
                                                    <div class="fw-semibold"><c:out value="${t.folioToca}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Expediente</div>
                                                    <div class="fw-semibold"><c:out value="${t.numeroExpediente}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Materia</div>
                                                    <div class="fw-semibold"><c:out value="${t.materiaNombre}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Sala</div>
                                                    <div class="fw-semibold"><c:out value="${t.salaNombre}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Recepción</div>
                                                    <div class="fw-semibold"><c:out value="${t.fechaRecepcionTexto}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Estado</div>
                                                    <div><n:estado valor="${t.estado.etiqueta()}"/></div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <a class="btn btn-sij-secundario"
                                               href="<c:url value='/segunda/tocas/historial?id=${t.id}'/>">
                                                Ver historial
                                            </a>
                                            <a class="btn btn-primary"
                                               href="<c:url value='/segunda/tocas/detalle?id=${t.id}'/>">
                                                Abrir toca completa
                                            </a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        
                    </c:forEach>
</c:otherwise>
        </c:choose>
    </n:tarjeta>

    <c:if test="${not empty pagina.content}">
        <div class="d-flex flex-wrap align-items-center gap-3 sin-paginacion">
            <form class="d-flex align-items-center gap-2" method="get" action="<c:url value='/segunda/tocas'/>">
                <input type="hidden" name="q" value="<c:out value='${q}'/>">
                <input type="hidden" name="estado" value="<c:out value='${estado}'/>">
                <input type="hidden" name="salaId" value="<c:out value='${salaId}'/>">
                <label for="size" class="sij-texto-chico text-nowrap">Registros por página</label>
                <select id="size" name="size" class="form-select form-select-sm w-auto" data-autoenvio>
                    <c:forEach var="tam" items="${tamanosPagina}">
                        <option value="<c:out value='${tam}'/>"${tam eq size ? ' selected' : ''}>
                            <c:out value="${tam}"/>
                        </option>
                    </c:forEach>
                </select>
            </form>
            <div class="flex-grow-1">
                <n:paginacion pagina="${pagina}" url="/segunda/tocas"/>
            </div>
        </div>
    </c:if>

        </div>
    </jsp:body>
</t:layout>
