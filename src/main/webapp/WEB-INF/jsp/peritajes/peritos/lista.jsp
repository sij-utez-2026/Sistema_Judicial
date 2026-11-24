<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<%-- RF 3.2 Catálogo de peritos (bocetos docs/modulos/peritajes-figma-catalogo.png y -ver.png) --%>
<t:layout titulo="Catálogo de peritos" modulo="peritajes" seccion="peritos">
    <jsp:attribute name="estilos">
        <link rel="stylesheet" href="<c:url value='/recursos/peritajes/css/peritajes.css'/>">
    </jsp:attribute>
    <jsp:attribute name="scripts">
        <script src="<c:url value='/recursos/peritajes/js/peritos.js'/>"></script>
    </jsp:attribute>
    <jsp:body>
        <n:migas>Catálogo de peritos</n:migas>

        <n:encabezado titulo="Catálogo de peritos"
                      descripcion="Padrón institucional, especialidades y disponibilidad para asignación.">
            <a class="btn btn-primary" href="<c:url value='/peritajes/peritos/nuevo'/>">+ Registrar perito</a>
        </n:encabezado>

        <%-- RF 3.1 (M2): contraseña temporal del perito recién registrado. Solo llega una vez (FlashMessage). --%>
        <c:if test="${not empty contrasenaTemporal}">
            <div class="sij-aviso sij-aviso-advertencia p-3 mt-3">
                <div class="fw-semibold">Contraseña temporal de <c:out value="${correoNuevo}"/></div>
                <div class="fs-5 font-monospace my-1"><c:out value="${contrasenaTemporal}"/></div>
                Entréguela al perito para su primer inicio de sesión. No se volverá a mostrar; el perito puede
                cambiarla en "Cambiar contraseña".
            </div>
        </c:if>

        <div class="d-flex flex-wrap gap-2 my-3">
            <span class="sij-contador">&bull; <c:out value="${totalRegistrados}"/> Registrados</span>
            <span class="sij-contador">&bull; <c:out value="${totalActivos}"/> Activos</span>
            <span class="sij-contador">&bull; <c:out value="${totalInactivos}"/> Inactivos</span>
        </div>

        <%-- Filtros: al cambiar una lista se aplica (data-autoenvio, comun.js); el texto se aplica con Enter --%>
        <form class="row g-2 mb-2" method="get" action="<c:url value='/peritajes/peritos'/>">
            <input type="hidden" name="size" value="<c:out value='${size}'/>">
            <div class="col-lg-4 col-md-6">
                <div class="input-group">
                    <button type="submit" class="btn btn-sij-secundario" aria-label="Buscar"><n:icono nombre="buscar" tamano="14"/></button>
                    <input type="search" class="form-control" name="q" value="<c:out value='${q}'/>"
                           placeholder="Buscar por nombre o número de registro" aria-label="Buscar">
                </div>
            </div>
            <div class="col-lg-3 col-md-6">
                <select class="form-select" name="especialidad" aria-label="Especialidad" data-autoenvio>
                    <option value="">Especialidad: Todas</option>
                    <c:forEach var="e" items="${especialidades}">
                        <option value="<c:out value='${e}'/>"${e eq especialidad ? ' selected' : ''}><c:out value="${e}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-3 col-md-6">
                <select class="form-select" name="materia" aria-label="Materia" data-autoenvio>
                    <option value="">Materia: Todas</option>
                    <c:forEach var="m" items="${materias}">
                        <option value="<c:out value='${m.clave}'/>"${m.clave eq materia ? ' selected' : ''}><c:out value="${m.nombre}"/></option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-lg-2 col-md-6">
                <select class="form-select" name="estado" aria-label="Estado" data-autoenvio>
                    <option value="">Estado: Todos</option>
                    <option value="activo"${estado eq 'activo' ? ' selected' : ''}>Activo</option>
                    <option value="inactivo"${estado eq 'inactivo' ? ' selected' : ''}>Inactivo</option>
                </select>
            </div>
        </form>

        <c:if test="${hayFiltros}">
            <div class="sij-texto-chico mb-3">
                Filtros aplicados · <c:out value="${pagina.totalElements}"/>
                <c:out value="${pagina.totalElements eq 1 ? 'resultado coincidente' : 'resultados coincidentes'}"/>
                · <a href="<c:url value='/peritajes/peritos'/>">Quitar filtros</a>
            </div>
        </c:if>

        <n:tarjeta sinRelleno="true">
            <c:choose>
                <c:when test="${empty pagina.content}">
                    <div class="p-4 text-center sij-descripcion">
                        <c:out value="${totalRegistrados eq 0 ? 'Aún no hay peritos registrados.' : 'No se encontraron peritos'}"/>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table sij-tabla align-middle mb-0">
                            <thead>
                            <tr>
                                <th>Perito / Registro</th>
                                <th>Materias</th>
                                <th>Estado</th>
                                <th>Carga activa</th>
                                <th class="text-end">Acciones</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="p" items="${pagina.content}">
                                <tr>
                                    <td>
                                        <div class="fw-semibold"><c:out value="${p.nombreCompleto}"/></div>
                                        <div class="sij-texto-chico"><c:out value="${p.numeroRegistro}"/> · <c:out value="${p.rol}"/></div>
                                    </td>
                                    <td><c:out value="${p.materiasTexto}"/></td>
                                    <td><n:estado valor="${p.activo ? 'Activo' : 'Inactivo'}"/></td>
                                    <td>
                                        <div class="fw-semibold fs-5"><c:out value="${p.cargaTotal}"/></div>
                                        <div class="sij-texto-chico">
                                            <c:out value="${p.cargaAsignados}"/> Asig. · <c:out value="${p.cargaEnProceso}"/> Proc.
                                        </div>
                                    </td>
                                    <td class="text-end">
                                        <a class="btn btn-sm btn-sij-secundario" title="Editar"
                                           href="<c:url value='/peritajes/peritos/editar?id=${p.id}'/>"
                                           aria-label="Editar a <c:out value='${p.nombreCompleto}'/>"><n:icono nombre="editar" tamano="14"/></a>
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
            <div class="d-flex flex-wrap align-items-center gap-3">
                <%-- RF 3.2: el administrador puede cambiar el número de registros por página --%>
                <form class="d-flex align-items-center gap-2" method="get" action="<c:url value='/peritajes/peritos'/>">
                    <input type="hidden" name="q" value="<c:out value='${q}'/>">
                    <input type="hidden" name="especialidad" value="<c:out value='${especialidad}'/>">
                    <input type="hidden" name="materia" value="<c:out value='${materia}'/>">
                    <input type="hidden" name="estado" value="<c:out value='${estado}'/>">
                    <label for="size" class="sij-texto-chico text-nowrap">Registros por página</label>
                    <select id="size" name="size" class="form-select form-select-sm w-auto" data-autoenvio>
                        <c:forEach var="t" items="${tamanosPagina}">
                            <option value="<c:out value='${t}'/>"${t eq size ? ' selected' : ''}><c:out value="${t}"/></option>
                        </c:forEach>
                    </select>
                </form>
                <div class="flex-grow-1">
                    <n:paginacion pagina="${pagina}" url="/peritajes/peritos"/>
                </div>
            </div>
        </c:if>
    </jsp:body>
</t:layout>
