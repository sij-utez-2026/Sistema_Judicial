<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, mx.edu.utez.oficialiaparte.model.Materia" %>
<%
    List<Materia> materias = (List<Materia>) request.getAttribute("materias");
    if (materias == null) materias = java.util.Collections.emptyList();

    String msg = request.getParameter("msg");
    String error = request.getParameter("error");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Materias · Sistema de Oficialía de Partes</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/recursos/nucleo/css/estilos.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/recursos/oficialia/css/oficialia.css">
</head>
<body class="sij-body">
<div class="sij-app">

    <!-- Barra lateral -->
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="materias"/>
    </jsp:include>

    <!-- Contenido Principal -->
    <div class="sij-contenido">
        <jsp:include page="topbar.jsp"/>

        <main class="sij-main" id="contenido">
            <!-- Encabezado de Sección (Captura 5) -->
            <div class="d-flex justify-content-between align-items-end mb-4 flex-wrap gap-2">
                <div>
                    <div class="sij-etiqueta-menu mt-0 mb-1" style="color: var(--sij-acento); font-size: 0.72rem; font-weight: 700; letter-spacing: 0.12em;">CATÁLOGO INSTITUCIONAL</div>
                    <h1 class="sij-titulo mt-0 mb-1" style="font-size: 2.1rem; font-weight: 600;">Materias</h1>
                    <p class="sij-descripcion" style="font-size: 0.95rem;">Gestiona el catálogo independiente de materias judiciales.</p>
                </div>
                <div>
                    <button type="button" class="btn btn-primary" onclick="openModal('modalNuevaMateria')">
                        + Nueva materia
                    </button>
                </div>
            </div>

            <!-- Avisos / Notificaciones -->
            <% if ("creado".equals(msg)) { %>
            <div class="sij-aviso p-3 mb-3 border-success text-success">
                &#10004; Materia judicial registrada exitosamente en el catálogo.
            </div>
            <% } else if ("actualizado".equals(msg)) { %>
            <div class="sij-aviso p-3 mb-3 border-success text-success">
                &#10004; Estado o datos de la materia actualizados correctamente.
            </div>
            <% } else if ("eliminado".equals(msg)) { %>
            <div class="sij-aviso p-3 mb-3 border-success text-success">
                &#10004; Materia eliminada satisfactoriamente.
            </div>
            <% } else if ("duplicado".equals(error)) { %>
            <div class="sij-aviso sij-aviso-advertencia p-3 mb-3 text-danger">
                &#9888; Ya existe una materia registrada con esa clave.
            </div>
            <% } %>

            <!-- Tarjeta con Tabla (Captura 5) -->
            <div class="sij-tarjeta p-0 overflow-hidden shadow-sm">
                <div class="d-flex justify-content-between align-items-center p-3 px-4 border-bottom bg-white flex-wrap gap-2">
                    <div class="ofi-search-wrapper" style="max-width: 400px; width: 100%;">
                        <div class="position-relative">
                            <span style="position: absolute; left: 1rem; top: 50%; transform: translateY(-50%); color: #8e8e84; pointer-events: none;">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <circle cx="11" cy="11" r="8"></circle>
                                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                                </svg>
                            </span>
                            <input type="text" class="form-control search-input" style="padding-left: 2.75rem;" placeholder="Buscar materia..." autofocus>
                        </div>
                    </div>
                    <div>
                        <span class="sij-texto-suave fs-7 table-records-badge"><%= materias.size() %> registros</span>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table sij-tabla align-middle mb-0 custom-table">
                        <thead>
                        <tr>
                            <th style="width: 140px;">CLAVE</th>
                            <th style="width: 220px;">NOMBRE</th>
                            <th>DESCRIPCIÓN</th>
                            <th style="width: 180px;">ESTADO</th>
                            <th style="width: 110px;" class="text-center">ACCIONES</th>
                        </tr>
                        </thead>
                        <tbody>
                        <% if (materias.isEmpty()) { %>
                        <tr>
                            <td colspan="5" class="text-center py-4 text-muted">
                                No se encontraron materias registradas.
                            </td>
                        </tr>
                        <% } else {
                            for (Materia m : materias) { %>
                        <tr>
                            <td>
                                <span class="ofi-badge-code"><%= m.getClave() %></span>
                            </td>
                            <td class="fw-semibold"><%= m.getNombre() %></td>
                            <td class="sij-texto-suave"><%= m.getDescripcion() != null ? m.getDescripcion() : "—" %></td>
                            <td>
                                <!-- Toggle switch interactivo según Boceto -->
                                <form action="${pageContext.request.contextPath}/materias" method="POST" class="ofi-switch-form">
                                    <input type="hidden" name="action" value="toggle">
                                    <input type="hidden" name="clave" value="<%= m.getClave() %>">
                                    <label class="ofi-switch" title="Cambiar estado">
                                        <input type="checkbox" <%= m.isActivo() ? "checked" : "" %> onchange="this.form.submit()">
                                        <span class="ofi-slider"></span>
                                        <span class="ofi-switch-text"><%= m.isActivo() ? "Activo" : "Inactivo" %></span>
                                    </label>
                                </form>
                            </td>
                            <td class="text-center">
                                <div class="d-inline-flex gap-2">
                                    <button type="button" class="ofi-btn-action" title="Editar materia"
                                            onclick="prepararEditarMateria('<%= m.getClave() %>', '<%= m.getNombre().replace("'", "\\'") %>', '<%= m.getDescripcion() != null ? m.getDescripcion().replace("'", "\\'") : "" %>', '<%= m.getEstado() %>')">
                                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                        </svg>
                                    </button>
                                    <button type="button" class="ofi-btn-action ofi-btn-action-danger" title="Eliminar materia"
                                            onclick="prepararEliminarMateria('<%= m.getClave() %>', '<%= m.getNombre().replace("'", "\\'") %>')">
                                        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                            <polyline points="3 6 5 6 21 6"></polyline>
                                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                        </svg>
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <%  }
                        } %>
                        </tbody>
                    </table>
                </div>

                <div class="p-3 px-4 border-top bg-white sij-texto-chico table-footer text-muted">
                    Mostrando <%= materias.size() %> de <%= materias.size() %> registros
                </div>
            </div>
        </main>

        <footer class="sij-pie text-center">
            Poder Judicial del Estado de Morelos · Sistema Judicial Integral &copy; 2026
        </footer>
    </div>

</div>

<!-- MODAL NUEVA MATERIA (Captura 2) -->
<div class="ofi-modal-backdrop" id="modalNuevaMateria">
    <div class="ofi-modal-dialog">
        <div class="d-flex justify-content-between align-items-center p-3 px-4 border-bottom bg-white">
            <div>
                <div class="sij-subtitulo-dorado text-uppercase fw-bold" style="letter-spacing: .08em; font-size: 0.72rem;">INFORMACIÓN DEL REGISTRO</div>
                <h2 class="h5 fw-bold mb-0 text-dark">Nueva materia</h2>
                <div class="text-muted fs-7">Completa los campos obligatorios para guardar.</div>
            </div>
            <button type="button" class="ofi-modal-close" data-close-modal aria-label="Cerrar">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/materias" method="POST">
            <input type="hidden" name="action" value="crear">
            <div class="p-4 bg-white">
                <div class="row g-3">
                    <div class="col-sm-6">
                        <label class="form-label fw-semibold fs-7" for="new-materia-clave">
                            Clave <span class="text-danger">*</span>
                        </label>
                        <input type="text" id="new-materia-clave" name="clave" class="form-control" placeholder="Ej. MAT-ADM" required>
                    </div>

                    <div class="col-sm-6">
                        <label class="form-label fw-semibold fs-7" for="new-materia-nombre">
                            Nombre de la materia <span class="text-danger">*</span>
                        </label>
                        <input type="text" id="new-materia-nombre" name="nombre" class="form-control" placeholder="Nombre" required>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold fs-7" for="new-materia-desc">
                            Descripción <span class="text-danger">*</span>
                        </label>
                        <textarea id="new-materia-desc" name="descripcion" class="form-control" rows="3" placeholder="Descripción breve de la materia" required></textarea>
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

<!-- MODAL EDITAR MATERIA -->
<div class="ofi-modal-backdrop" id="modalEditarMateria">
    <div class="ofi-modal-dialog">
        <div class="d-flex justify-content-between align-items-center p-3 px-4 border-bottom bg-white">
            <div>
                <div class="sij-subtitulo-dorado text-uppercase fw-bold" style="letter-spacing: .08em; font-size: 0.72rem;">INFORMACIÓN DEL REGISTRO</div>
                <h2 class="h5 fw-bold mb-0 text-dark">Editar materia</h2>
                <div class="text-muted fs-7">Completa los campos obligatorios para guardar.</div>
            </div>
            <button type="button" class="ofi-modal-close" data-close-modal aria-label="Cerrar">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/materias" method="POST">
            <input type="hidden" name="action" value="editar">
            <input type="hidden" id="edit-materia-clave-hidden" name="clave">
            <input type="hidden" id="edit-materia-estado" name="estado">

            <div class="p-4 bg-white">
                <div class="row g-3">
                    <div class="col-sm-6">
                        <label class="form-label fw-semibold fs-7">Clave</label>
                        <input type="text" id="edit-materia-clave-display" class="form-control" readonly style="background-color: #f7f5ed;">
                    </div>

                    <div class="col-sm-6">
                        <label class="form-label fw-semibold fs-7" for="edit-materia-nombre">
                            Nombre de la materia <span class="text-danger">*</span>
                        </label>
                        <input type="text" id="edit-materia-nombre" name="nombre" class="form-control" required>
                    </div>

                    <div class="col-12">
                        <label class="form-label fw-semibold fs-7" for="edit-materia-desc">
                            Descripción <span class="text-danger">*</span>
                        </label>
                        <textarea id="edit-materia-desc" name="descripcion" class="form-control" rows="3" required></textarea>
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

<!-- MODAL ELIMINAR MATERIA -->
<div class="ofi-modal-backdrop" id="modalEliminarMateria">
    <div class="ofi-modal-dialog" style="max-width: 440px;">
        <div class="d-flex justify-content-between align-items-center p-3 px-4 border-bottom bg-white">
            <h2 class="h5 fw-semibold mb-0 text-danger">&#9888; ¿Eliminar materia?</h2>
            <button type="button" class="ofi-modal-close" data-close-modal aria-label="Cerrar">&times;</button>
        </div>

        <form action="${pageContext.request.contextPath}/materias" method="POST">
            <input type="hidden" name="action" value="eliminar">
            <input type="hidden" id="delete-materia-clave" name="clave">

            <div class="p-4 bg-white">
                <p class="sij-texto-suave mb-2" style="font-size: .88rem;">
                    Esta acción dará de baja la materia seleccionada del catálogo judicial:
                </p>
                <div id="delete-materia-nombre" class="p-2 px-3 rounded bg-light border text-danger fw-semibold">
                </div>
            </div>

            <div class="d-flex justify-content-end gap-2 p-3 px-4 border-top bg-white">
                <button type="button" class="btn btn-sij-secundario" data-close-modal>Cancelar</button>
                <button type="submit" class="btn btn-danger">Eliminar definitivamente</button>
            </div>
        </form>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>
