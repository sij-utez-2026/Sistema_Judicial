<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- RF 4.2 Listado de magistrados con modales de edición, detalle y cambio de estado --%>
<t:layout titulo="Magistrados" modulo="segunda" seccion="magistrados">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-magistrados-lista">
    <n:migas>Magistrados</n:migas>

    <n:encabezado titulo="Magistrados"
                  descripcion="Magistrados de segunda instancia y su sala asignada.">
        <a class="btn btn-primary" href="<c:url value='/segunda/magistrados/nuevo'/>"><n:icono nombre="mas" tamano="14"/> Registrar magistrado</a>
    </n:encabezado>

    <div class="d-flex flex-wrap gap-2 my-3 sin-resumen">
        <span class="sij-contador">&bull; <c:out value="${totalRegistrados}"/> Registrados</span>
        <span class="sij-contador">&bull; <c:out value="${totalActivos}"/> Activos</span>
        <span class="sij-contador">&bull; <c:out value="${totalInactivos}"/> Inactivos</span>
    </div>

    <form class="row g-2 mb-2 sin-filtros" method="get" action="<c:url value='/segunda/magistrados'/>">
        <input type="hidden" name="size" value="<c:out value='${size}'/>">
        <div class="col-lg-6 col-md-6">
            <div class="input-group">
                <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar">
                    <n:icono nombre="buscar" tamano="14"/>
                </button>
                <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
                       placeholder="Buscar por nombre o correo" aria-label="Buscar">
            </div>
        </div>
        <div class="col-lg-3 col-md-6">
            <select class="form-select" name="estado" aria-label="Estado" data-autoenvio>
                <option value="">Estado: Todos</option>
                <option value="activo"${estado eq 'activo' ? ' selected' : ''}>Activo</option>
                <option value="inactivo"${estado eq 'inactivo' ? ' selected' : ''}>Inactivo</option>
            </select>
        </div>
    </form>

    <c:if test="${hayFiltros}">
        <div class="sij-texto-chico mb-3 sin-filtros-aplicados">
            Filtros aplicados · <c:out value="${pagina.totalElements}"/>
            <c:out value="${pagina.totalElements eq 1 ? 'resultado coincidente' : 'resultados coincidentes'}"/>
            · <a href="<c:url value='/segunda/magistrados'/>">Quitar filtros</a>
        </div>
    </c:if>

    <n:tarjeta sinRelleno="true" clase="sin-listado">
        <c:choose>
            <c:when test="${empty pagina.content}">
                <div class="p-4 text-center sij-descripcion">
                    <c:out value="${totalRegistrados eq 0 ? 'Aún no hay magistrados registrados.' : 'No se encontraron magistrados'}"/>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table sij-tabla align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Magistrado</th>
                            <th>Correo</th>
                            <th>Sala</th>
                            <th>Estado</th>
                            <th class="text-end">Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="m" items="${pagina.content}">
                            <tr>
                                <td class="fw-semibold"><c:out value="${m.nombreCompleto}"/></td>
                                <td><c:out value="${m.correo}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${m.sinSala}">
                                            <span class="sij-texto-chico text-muted">Sin sala</span>
                                        </c:when>
                                        <c:otherwise><c:out value="${m.salaNombre}"/></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><n:estado valor="${m.activo ? 'Activo' : 'Inactivo'}"/></td>
                                <td class="text-end">
                                    <div class="btn-group" role="group">
                                        <button type="button" class="btn btn-sm btn-sij-secundario"
                                                data-bs-toggle="modal" data-bs-target="#modalDetalle${m.id}"
                                                title="Ver detalle">
                                            <n:icono nombre="documento" tamano="14"/>
                                        </button>
                                        <button type="button" class="btn btn-sm btn-sij-secundario"
                                                data-bs-toggle="modal" data-bs-target="#modalEditar${m.id}"
                                                title="Editar">
                                            <n:icono nombre="editar" tamano="14"/>
                                        </button>
                                        <form method="post" action="<c:url value='/segunda/magistrados/estado'/>"
                                              class="d-inline"
                                              data-confirmar="¿<c:out value='${m.activo ? "Dar de baja" : "Reactivar"}'/> a <c:out value='${m.nombreCompleto}'/>?">
                                            <n:csrf/>
                                            <input type="hidden" name="id" value="<c:out value='${m.id}'/>">
                                            <input type="hidden" name="activo" value="<c:out value='${not m.activo}'/>">
                                            <button type="submit" class="btn btn-sm btn-sij-secundario"
                                                    title="${m.activo ? 'Dar de baja' : 'Reactivar'}">
                                                <n:icono nombre="${m.activo ? 'salir' : 'mas'}" tamano="14"/>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>

                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            
                    <c:forEach var="m" items="${pagina.content}">
<%-- ============ MODAL DETALLE ============ --%>
                            <div class="modal fade" id="modalDetalle${m.id}" tabindex="-1"
                                 aria-labelledby="tituloDetalle${m.id}" aria-hidden="true">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h5 class="modal-title" id="tituloDetalle${m.id}">
                                                <c:out value="${m.nombreCompleto}"/>
                                            </h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"
                                                    aria-label="Cerrar"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="row g-3 sij-texto-chico sin-datos">
                                                <div class="col-md-6">
                                                    <div class="text-muted">Nombre</div>
                                                    <div class="fw-semibold"><c:out value="${m.nombreCompleto}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Estado</div>
                                                    <div><n:estado valor="${m.activo ? 'Activo' : 'Inactivo'}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Correo</div>
                                                    <div class="fw-semibold"><c:out value="${m.correo}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Sala</div>
                                                    <div class="fw-semibold">
                                                        <c:choose>
                                                            <c:when test="${m.sinSala}">
                                                                <span class="text-muted">Sin sala</span>
                                                            </c:when>
                                                            <c:otherwise><c:out value="${m.salaNombre}"/></c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <a class="btn btn-sij-secundario"
                                               href="<c:url value='/segunda/magistrados/detalle?id=${m.id}'/>">
                                                Ver tocas del magistrado
                                            </a>
                                            <a class="btn btn-primary"
                                               href="<c:url value='/segunda/magistrados/editar?id=${m.id}'/>">
                                                Edición completa y sala
                                            </a>
                                            <button type="button" class="btn btn-primary" data-bs-dismiss="modal">
                                                Cerrar
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <%-- ============ MODAL EDITAR (datos simples) ============ --%>
                            <div class="modal fade" id="modalEditar${m.id}" tabindex="-1"
                                 aria-labelledby="tituloEditar${m.id}" aria-hidden="true">
                                <div class="modal-dialog">
                                    <form method="post"
                                          action="<c:url value='/segunda/magistrados/editar?id=${m.id}'/>" novalidate>
                                        <n:csrf/>
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <h5 class="modal-title" id="tituloEditar${m.id}">
                                                    Editar magistrado
                                                </h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal"
                                                        aria-label="Cerrar"></button>
                                            </div>
                                            <div class="modal-body">
                                                <div class="mb-3">
                                                    <label for="nombre${m.id}" class="form-label">Nombre(s) *</label>
                                                    <input id="nombre${m.id}" name="nombre" type="text"
                                                           maxlength="80" required class="form-control"
                                                           value="<c:out value='${m.nombre}'/>">
                                                </div>
                                                <div class="mb-3">
                                                    <label for="apellidos${m.id}" class="form-label">Apellidos *</label>
                                                    <input id="apellidos${m.id}" name="apellidos" type="text"
                                                           maxlength="120" required class="form-control"
                                                           value="<c:out value='${m.apellidos}'/>">
                                                </div>
                                                <div class="mb-3">
                                                    <label class="form-label">Correo</label>
                                                    <input type="email" name="correo" readonly class="form-control"
                                                           value="<c:out value='${m.correo}'/>">
                                                    <div class="form-text">No se puede cambiar.</div>
                                                </div>
                                                <div class="sij-texto-chico text-muted">
                                                    La asignación de sala se administra desde la edición completa.
                                                </div>
                                            </div>
                                            <div class="modal-footer">
                                                <button type="button" class="btn btn-sij-secundario"
                                                        data-bs-dismiss="modal">Cancelar</button>
                                                <button type="submit" class="btn btn-primary">Guardar</button>
                                            </div>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        
                    </c:forEach>
</c:otherwise>
        </c:choose>
    </n:tarjeta>

    <c:if test="${not empty pagina.content}">
        <div class="d-flex flex-wrap align-items-center gap-3 sin-paginacion">
            <form class="d-flex align-items-center gap-2" method="get" action="<c:url value='/segunda/magistrados'/>">
                <input type="hidden" name="q" value="<c:out value='${q}'/>">
                <input type="hidden" name="estado" value="<c:out value='${estado}'/>">
                <label for="size" class="sij-texto-chico text-nowrap">Registros por página</label>
                <select id="size" name="size" class="form-select form-select-sm w-auto" data-autoenvio>
                    <c:forEach var="t" items="${tamanosPagina}">
                        <option value="<c:out value='${t}'/>"${t eq size ? ' selected' : ''}>
                            <c:out value="${t}"/>
                        </option>
                    </c:forEach>
                </select>
            </form>
            <div class="flex-grow-1">
                <n:paginacion pagina="${pagina}" url="/segunda/magistrados"/>
            </div>
        </div>
    </c:if>

        </div>
    </jsp:body>
</t:layout>
