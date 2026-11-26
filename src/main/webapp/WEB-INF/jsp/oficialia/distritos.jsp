<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <c:if test="${not empty _csrf}">
        <meta name="_csrf" content="<c:out value='${_csrf.token}'/>">
        <meta name="_csrf_header" content="<c:out value='${_csrf.headerName}'/>">
    </c:if>
    <title>Distritos · Oficialía de Partes</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="<c:url value='/recursos/nucleo/css/estilos.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/recursos/oficialia/css/oficialia.css?v=2'/>">
</head>
<body class="sij-body">
<div class="sij-app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="distritos"/>
    </jsp:include>

    <div class="sij-contenido">
        <jsp:include page="topbar.jsp"/>

        <main class="sij-main" id="contenido">
            <n:mensajes/>

            <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-2">
                <div>
                    <div class="ofi-eyebrow mb-1">ESTRUCTURA JUDICIAL</div>
                    <h1 class="sij-titulo mt-0 mb-1" style="font-size: 2rem; font-weight: 600;">Distritos</h1>
                    <p class="sij-descripcion mb-0" style="font-size: 0.95rem;">Administra los distritos judiciales registrados en el sistema.</p>
                </div>
                <div>
                    <button type="button" class="btn btn-primary" data-open-modal="modalNuevoDistrito">
                        + Nuevo distrito
                    </button>
                </div>
            </div>

            <div class="sij-tarjeta p-0 overflow-hidden shadow-sm">
                <div class="d-flex justify-content-between align-items-center p-3 px-4 border-bottom bg-white flex-wrap gap-2">
                    <div class="ofi-search-wrapper" style="max-width: 400px; width: 100%;">
                        <div class="position-relative">
                            <span style="position: absolute; left: 0.9rem; top: 50%; transform: translateY(-50%); color: var(--sij-texto-suave); pointer-events: none;">
                                <n:icono nombre="buscar" tamano="15"/>
                            </span>
                            <input type="text" class="form-control search-input" style="padding-left: 2.5rem;" placeholder="Buscar distrito..." autofocus>
                        </div>
                    </div>
                    <div>
                        <span class="sij-texto-chico table-records-badge"><c:out value="${distritos.size()}"/> registros</span>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table sij-tabla align-middle mb-0">
                        <thead>
                        <tr>
                            <th style="width: 140px;">CLAVE</th>
                            <th>NOMBRE DEL DISTRITO</th>
                            <th>CABECERA</th>
                            <th style="width: 170px;">ESTADO</th>
                            <th style="width: 110px;" class="text-center">ACCIONES</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${empty distritos}">
                                <tr>
                                    <td colspan="5" class="text-center py-4 text-muted">
                                        No se encontraron distritos registrados.
                                    </td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach var="d" items="${distritos}">
                                    <tr data-fila>
                                        <td>
                                            <span class="ofi-badge-code"><c:out value="${d.clave}"/></span>
                                        </td>
                                        <td class="fw-semibold"><c:out value="${d.nombre}"/></td>
                                        <td class="sij-texto-suave"><c:out value="${empty d.cabecera ? '—' : d.cabecera}"/></td>
                                        <td>
                                            <form action="<c:url value='/oficialia/distritos'/>" method="post" class="ofi-switch-form">
                                                <n:csrf/>
                                                <input type="hidden" name="action" value="toggle">
                                                <input type="hidden" name="clave" value="<c:out value='${d.clave}'/>">
                                                <label class="ofi-switch" title="Cambiar estado">
                                                    <input type="checkbox"${d.activo ? ' checked' : ''} onchange="this.form.submit()">
                                                    <span class="ofi-slider"></span>
                                                    <span class="ofi-switch-text"><c:out value="${d.estadoTexto}"/></span>
                                                </label>
                                            </form>
                                        </td>
                                        <td class="text-center">
                                            <div class="d-inline-flex gap-2">
                                                <button type="button" class="ofi-btn-action" title="Editar distrito"
                                                        data-editar-distrito
                                                        data-clave="<c:out value='${d.clave}'/>"
                                                        data-nombre="<c:out value='${d.nombre}'/>"
                                                        data-cabecera="<c:out value='${d.cabecera}'/>">
                                                    <n:icono nombre="editar" tamano="15"/>
                                                </button>
                                                <button type="button" class="ofi-btn-action ofi-btn-action-danger" title="Eliminar distrito"
                                                        data-eliminar-distrito
                                                        data-clave="<c:out value='${d.clave}'/>"
                                                        data-nombre="<c:out value='${d.nombre}'/>">
                                                    &#128465;
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>

                <div class="p-3 px-4 border-top bg-white sij-texto-chico table-footer text-muted">
                    Mostrando <c:out value="${distritos.size()}"/> de <c:out value="${distritos.size()}"/> registros
                </div>
            </div>
        </main>

        <footer class="sij-pie text-center">
            Poder Judicial del Estado de Morelos · Sistema Judicial Integral &copy; 2026
        </footer>
    </div>
</div>

<!-- MODAL NUEVO DISTRITO -->
<div class="ofi-modal-backdrop" id="modalNuevoDistrito">
    <div class="ofi-modal-dialog">
        <div class="d-flex justify-content-between align-items-center p-3 px-4 border-bottom bg-white">
            <div>
                <div class="sij-subtitulo-dorado text-uppercase fw-bold" style="letter-spacing: .08em; font-size: 0.72rem;">INFORMACIÓN DEL REGISTRO</div>
                <h2 class="h5 fw-bold mb-0 text-dark">Nuevo distrito</h2>
                <div class="text-muted small">Completa los campos obligatorios para guardar.</div>
            </div>
            <button type="button" class="ofi-modal-close" data-close-modal aria-label="Cerrar">&times;</button>
        </div>

        <form action="<c:url value='/oficialia/distritos'/>" method="post">
            <n:csrf/>
            <input type="hidden" name="action" value="crear">
            <div class="p-4 bg-white">
                <div class="row g-3">
                    <div class="col-sm-5">
                        <label class="form-label fw-semibold" for="new-distrito-clave">
                            Clave <span class="text-danger">*</span>
                        </label>
                        <input type="text" id="new-distrito-clave" name="clave" maxlength="10" class="form-control" placeholder="Ej. D03" required>
                    </div>
                    <div class="col-sm-7">
                        <label class="form-label fw-semibold" for="new-distrito-nombre">
                            Nombre del distrito <span class="text-danger">*</span>
                        </label>
                        <input type="text" id="new-distrito-nombre" name="nombre" maxlength="100" class="form-control" placeholder="Ej. Tercer Distrito Judicial" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold" for="new-distrito-cabecera">
                            Cabecera distrital
                        </label>
                        <input type="text" id="new-distrito-cabecera" name="cabecera" maxlength="150" class="form-control" placeholder="Ej. Cuautla">
                    </div>
                </div>
            </div>

            <div class="d-flex justify-content-end gap-2 p-3 px-4 border-top bg-white">
                <button type="button" class="btn btn-sij-secundario" data-close-modal>Cancelar</button>
                <button type="submit" class="btn btn-primary">Guardar registro</button>
            </div>
        </form>
    </div>
</div>

<!-- MODAL EDITAR DISTRITO -->
<div class="ofi-modal-backdrop" id="modalEditarDistrito">
    <div class="ofi-modal-dialog">
        <div class="d-flex justify-content-between align-items-center p-3 px-4 border-bottom bg-white">
            <div>
                <div class="sij-subtitulo-dorado text-uppercase fw-bold" style="letter-spacing: .08em; font-size: 0.72rem;">INFORMACIÓN DEL REGISTRO</div>
                <h2 class="h5 fw-bold mb-0 text-dark">Editar distrito</h2>
                <div class="text-muted small">Completa los campos obligatorios para guardar.</div>
            </div>
            <button type="button" class="ofi-modal-close" data-close-modal aria-label="Cerrar">&times;</button>
        </div>

        <form action="<c:url value='/oficialia/distritos'/>" method="post">
            <n:csrf/>
            <input type="hidden" name="action" value="editar">
            <input type="hidden" id="edit-distrito-clave-hidden" name="clave">

            <div class="p-4 bg-white">
                <div class="row g-3">
                    <div class="col-sm-5">
                        <label class="form-label fw-semibold">Clave</label>
                        <input type="text" id="edit-distrito-clave-display" class="form-control" readonly>
                    </div>
                    <div class="col-sm-7">
                        <label class="form-label fw-semibold" for="edit-distrito-nombre">
                            Nombre del distrito <span class="text-danger">*</span>
                        </label>
                        <input type="text" id="edit-distrito-nombre" name="nombre" maxlength="100" class="form-control" required>
                    </div>
                    <div class="col-12">
                        <label class="form-label fw-semibold" for="edit-distrito-cabecera">
                            Cabecera distrital
                        </label>
                        <input type="text" id="edit-distrito-cabecera" name="cabecera" maxlength="150" class="form-control">
                    </div>
                </div>
            </div>

            <div class="d-flex justify-content-end gap-2 p-3 px-4 border-top bg-white">
                <button type="button" class="btn btn-sij-secundario" data-close-modal>Cancelar</button>
                <button type="submit" class="btn btn-primary">Guardar cambios</button>
            </div>
        </form>
    </div>
</div>

<!-- MODAL ELIMINAR DISTRITO -->
<div class="ofi-modal-backdrop" id="modalEliminarDistrito">
    <div class="ofi-modal-dialog" style="max-width: 440px;">
        <div class="d-flex justify-content-between align-items-center p-3 px-4 border-bottom bg-white">
            <h2 class="h5 fw-semibold mb-0 text-danger">&#9888; ¿Eliminar distrito?</h2>
            <button type="button" class="ofi-modal-close" data-close-modal aria-label="Cerrar">&times;</button>
        </div>

        <form action="<c:url value='/oficialia/distritos'/>" method="post">
            <n:csrf/>
            <input type="hidden" name="action" value="eliminar">
            <input type="hidden" id="delete-distrito-clave" name="clave">

            <div class="p-4 bg-white">
                <p class="sij-texto-suave mb-2" style="font-size: .88rem;">
                    Esta acción dará de baja el distrito seleccionado del catálogo judicial:
                </p>
                <div id="delete-distrito-nombre" class="p-2 px-3 rounded bg-light border text-danger fw-semibold"></div>
            </div>

            <div class="d-flex justify-content-end gap-2 p-3 px-4 border-top bg-white">
                <button type="button" class="btn btn-sij-secundario" data-close-modal>Cancelar</button>
                <button type="submit" class="btn btn-danger">Eliminar definitivamente</button>
            </div>
        </form>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<c:url value='/recursos/nucleo/js/comun.js?v=2'/>"></script>
<script src="<c:url value='/recursos/oficialia/js/main.js?v=2'/>"></script>
</body>
</html>
