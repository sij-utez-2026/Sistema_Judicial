<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- RF 3.2 Listado de salas con modales de edición, detalle y cambio de estado --%>
<t:layout titulo="Salas" modulo="segunda" seccion="salas">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/segunda/css/pantallas.css'/>">
    </jsp:attribute>
    <jsp:body>
        <div class="sin-pantalla sin-vista-salas-lista">
    <n:migas>Salas</n:migas>

    <n:encabezado titulo="Salas"
                  descripcion="Salas de segunda instancia por distrito judicial y su magistrado.">
        <a class="btn btn-primary" href="<c:url value='/segunda/salas/nueva'/>"><n:icono nombre="mas" tamano="14"/> Registrar sala</a>
    </n:encabezado>

    <div class="d-flex flex-wrap gap-2 my-3 sin-resumen">
        <span class="sij-contador">&bull; <c:out value="${totalRegistradas}"/> Registradas</span>
        <span class="sij-contador">&bull; <c:out value="${totalActivas}"/> Activas</span>
        <span class="sij-contador">&bull; <c:out value="${totalInactivas}"/> Inactivas</span>
    </div>

    <%-- Filtros --%>
    <form class="row g-2 mb-2 sin-filtros" method="get" action="<c:url value='/segunda/salas'/>">
        <input type="hidden" name="size" value="<c:out value='${size}'/>">
        <div class="col-lg-5 col-md-6">
            <div class="input-group">
                <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar">
                    <n:icono nombre="buscar" tamano="14"/>
                </button>
                <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
                       placeholder="Buscar por nombre de sala" aria-label="Buscar">
            </div>
        </div>
        <div class="col-lg-3 col-md-6">
            <select class="form-select" name="distritoId" aria-label="Distrito" data-autoenvio>
                <option value="">Distrito: Todos</option>
                <c:forEach var="d" items="${distritos}">
                    <option value="<c:out value='${d.clave}'/>"${d.clave eq distritoId ? ' selected' : ''}>
                        <c:out value="${d.nombre}"/>
                    </option>
                </c:forEach>
            </select>
        </div>
        <div class="col-lg-2 col-md-6">
            <select class="form-select" name="estado" aria-label="Estado" data-autoenvio>
                <option value="">Estado: Todos</option>
                <option value="activa"${estado eq 'activa' ? ' selected' : ''}>Activa</option>
                <option value="inactiva"${estado eq 'inactiva' ? ' selected' : ''}>Inactiva</option>
            </select>
        </div>
    </form>

    <c:if test="${hayFiltros}">
        <div class="sij-texto-chico mb-3 sin-filtros-aplicados">
            Filtros aplicados · <c:out value="${pagina.totalElements}"/>
            <c:out value="${pagina.totalElements eq 1 ? 'resultado coincidente' : 'resultados coincidentes'}"/>
            · <a href="<c:url value='/segunda/salas'/>">Quitar filtros</a>
        </div>
    </c:if>

    <n:tarjeta sinRelleno="true" clase="sin-listado">
        <c:choose>
            <c:when test="${empty pagina.content}">
                <div class="p-4 text-center sij-descripcion">
                    <c:out value="${totalRegistradas eq 0 ? 'Aún no hay salas registradas.' : 'No se encontraron salas'}"/>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-responsive">
                    <table class="table sij-tabla align-middle mb-0">
                        <thead>
                        <tr>
                            <th>Sala</th>
                            <th>Distrito</th>
                            <th>Magistrado</th>
                            <th>Estado</th>
                            <th class="text-end">Acciones</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="s" items="${pagina.content}">
                            <tr>
                                <td>
                                    <div class="fw-semibold"><c:out value="${s.nombre}"/></div>
                                    <div class="sij-texto-chico"><c:out value="${s.direccion}"/></div>
                                </td>
                                <td><c:out value="${s.distritoNombre}"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.sinMagistrado}">
                                            <span class="sij-texto-chico text-muted">Sin magistrado</span>
                                        </c:when>
                                        <c:otherwise><c:out value="${s.magistradoNombre}"/></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><n:estado valor="${s.activa ? 'Activa' : 'Inactiva'}"/></td>
                                <td class="text-end">
                                    <div class="btn-group" role="group">
                                            <%-- Ver detalle en modal --%>
                                        <button type="button" class="btn btn-sm btn-sij-secundario"
                                                data-bs-toggle="modal" data-bs-target="#modalDetalle${s.id}"
                                                title="Ver detalle">
                                            <n:icono nombre="documento" tamano="14"/>
                                        </button>

                                            <%-- Editar en modal --%>
                                        <button type="button" class="btn btn-sm btn-sij-secundario"
                                                data-bs-toggle="modal" data-bs-target="#modalEditar${s.id}"
                                                title="Editar">
                                            <n:icono nombre="editar" tamano="14"/>
                                        </button>

                                            <%-- Desactivar / Reactivar (POST inline con confirmación) --%>
                                        <form method="post" action="<c:url value='/segunda/salas/estado'/>"
                                              class="d-inline"
                                              data-confirmar="¿<c:out value='${s.activa ? "Desactivar" : "Reactivar"}'/> la sala <c:out value='${s.nombre}'/>?">
                                            <n:csrf/>
                                            <input type="hidden" name="id" value="<c:out value='${s.id}'/>">
                                            <input type="hidden" name="activa" value="<c:out value='${not s.activa}'/>">
                                            <button type="submit" class="btn btn-sm btn-sij-secundario"
                                                    title="${s.activa ? 'Desactivar' : 'Reactivar'}">
                                                <n:icono nombre="${s.activa ? 'salir' : 'mas'}" tamano="14"/>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>

                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            
                    <c:forEach var="s" items="${pagina.content}">
<%-- ============ MODAL DETALLE ============ --%>
                            <div class="modal fade" id="modalDetalle${s.id}" tabindex="-1"
                                 aria-labelledby="tituloDetalle${s.id}" aria-hidden="true">
                                <div class="modal-dialog modal-lg">
                                    <div class="modal-content">
                                        <div class="modal-header">
                                            <h5 class="modal-title" id="tituloDetalle${s.id}">
                                                Sala <c:out value="${s.nombre}"/>
                                            </h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal"
                                                    aria-label="Cerrar"></button>
                                        </div>
                                        <div class="modal-body">
                                            <div class="row g-3 sij-texto-chico sin-datos">
                                                <div class="col-md-6">
                                                    <div class="text-muted">Nombre</div>
                                                    <div class="fw-semibold"><c:out value="${s.nombre}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Estado</div>
                                                    <div><n:estado valor="${s.activa ? 'Activa' : 'Inactiva'}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Distrito</div>
                                                    <div class="fw-semibold"><c:out value="${s.distritoNombre}"/></div>
                                                </div>
                                                <div class="col-md-6">
                                                    <div class="text-muted">Magistrado</div>
                                                    <div class="fw-semibold">
                                                        <c:choose>
                                                            <c:when test="${s.sinMagistrado}">
                                                                <span class="text-muted">Sin magistrado</span>
                                                            </c:when>
                                                            <c:otherwise><c:out value="${s.magistradoNombre}"/></c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </div>
                                                <div class="col-12">
                                                    <div class="text-muted">Dirección</div>
                                                    <div class="fw-semibold"><c:out value="${s.direccion}"/></div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="modal-footer">
                                            <a class="btn btn-sij-secundario"
                                               href="<c:url value='/segunda/salas/detalle?id=${s.id}'/>">
                                                Ver tocas de esta sala
                                            </a>
                                            <button type="button" class="btn btn-primary" data-bs-dismiss="modal">
                                                Cerrar
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <%-- ============ MODAL EDITAR ============ --%>
                            <div class="modal fade" id="modalEditar${s.id}" tabindex="-1"
                                 aria-labelledby="tituloEditar${s.id}" aria-hidden="true">
                                <div class="modal-dialog">
                                    <form method="post"
                                          action="<c:url value='/segunda/salas/editar?id=${s.id}'/>" novalidate>
                                        <n:csrf/>
                                        <div class="modal-content">
                                            <div class="modal-header">
                                                <h5 class="modal-title" id="tituloEditar${s.id}">
                                                    Editar sala
                                                </h5>
                                                <button type="button" class="btn-close" data-bs-dismiss="modal"
                                                        aria-label="Cerrar"></button>
                                            </div>
                                            <div class="modal-body">
                                                <div class="mb-3">
                                                    <label for="nombre${s.id}" class="form-label">Nombre *</label>
                                                    <input id="nombre${s.id}" name="nombre" type="text"
                                                           maxlength="100" required class="form-control"
                                                           value="<c:out value='${s.nombre}'/>">
                                                </div>
                                                <div class="mb-3">
                                                    <label for="distritoId${s.id}" class="form-label">Distrito *</label>
                                                    <select id="distritoId${s.id}" name="distritoId"
                                                            required class="form-select">
                                                        <c:forEach var="d" items="${distritos}">
                                                            <option value="<c:out value='${d.clave}'/>"${d.clave eq s.distritoId ? ' selected' : ''}>
                                                                <c:out value="${d.nombre}"/>
                                                            </option>
                                                        </c:forEach>
                                                    </select>
                                                    <div class="form-text">No se puede cambiar si la sala tiene tocas asignadas.</div>
                                                </div>
                                                <div class="mb-3">
                                                    <label for="direccion${s.id}" class="form-label">Dirección *</label>
                                                    <input id="direccion${s.id}" name="direccion" type="text"
                                                           maxlength="255" required class="form-control"
                                                           value="<c:out value='${s.direccion}'/>">
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
            <form class="d-flex align-items-center gap-2" method="get" action="<c:url value='/segunda/salas'/>">
                <input type="hidden" name="q" value="<c:out value='${q}'/>">
                <input type="hidden" name="distritoId" value="<c:out value='${distritoId}'/>">
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
                <n:paginacion pagina="${pagina}" url="/segunda/salas"/>
            </div>
        </div>
    </c:if>

        </div>
    </jsp:body>
</t:layout>

