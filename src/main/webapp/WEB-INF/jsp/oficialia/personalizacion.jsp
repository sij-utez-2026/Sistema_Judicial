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
    <title>Personalización · Oficialía de Partes</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="<c:url value='/recursos/nucleo/css/estilos.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/recursos/oficialia/css/oficialia.css?v=2'/>">
</head>
<body class="sij-body">
<div class="sij-app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="personalizacion"/>
    </jsp:include>

    <div class="sij-contenido">
        <jsp:include page="topbar.jsp"/>

        <main class="sij-main" id="contenido">
            <n:mensajes/>

            <div class="mb-4">
                <div class="ofi-eyebrow mb-1">IDENTIDAD INSTITUCIONAL</div>
                <h1 class="sij-titulo mt-0 mb-1" style="font-size: 2rem; font-weight: 600;">Personalización</h1>
                <p class="sij-descripcion mb-0" style="font-size: 0.95rem;">
                    Consulta el logotipo, los colores institucionales y los datos del administrador en sesión.
                </p>
            </div>

            <form action="<c:url value='/oficialia/personalizacion'/>" method="post">
                <n:csrf/>
                <div class="row g-4">
                    <div class="col-lg-7">
                        <div class="mb-4">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="ofi-step-pill">01</span>
                                <h2 class="h5 fw-bold mb-0 text-dark">Logotipo institucional</h2>
                            </div>
                            <p class="sij-texto-suave mb-3" style="font-size: 0.85rem; padding-left: 34px;">
                                Formatos PNG o SVG. Tamaño máximo sugerido: 2 MB.
                            </p>

                            <div class="ofi-logo-box">
                                <div class="d-flex align-items-center gap-3">
                                    <div class="ofi-circle-logo dark" style="width: 46px; height: 46px; font-size: 1rem; border-width: 1.5px;">PJ</div>
                                    <div class="ofi-pj-text">
                                        <div class="ofi-pj-title text-dark" style="font-size: 0.88rem; font-weight: 700;">PODER JUDICIAL</div>
                                        <div class="ofi-pj-sub text-muted" style="font-size: 0.68rem;">DEL ESTADO DE MORELOS</div>
                                        <div id="nombre-archivo-logo" class="sij-texto-chico mt-1"></div>
                                    </div>
                                </div>
                                <div>
                                    <button type="button" class="btn btn-sij-secundario shadow-sm d-flex align-items-center gap-2"
                                            onclick="document.getElementById('input-logo-file').click()">
                                        <span>Cambiar logotipo</span>
                                    </button>
                                    <input type="file" id="input-logo-file" style="display: none;" accept="image/png, image/svg+xml">
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="ofi-step-pill">02</span>
                                <h2 class="h5 fw-bold mb-0 text-dark">Colores de la interfaz (Sistema Judicial Integral)</h2>
                            </div>
                            <p class="sij-texto-suave mb-3" style="font-size: 0.85rem; padding-left: 34px;">
                                Paleta institucional unificada con Peritajes y el núcleo del sistema.
                            </p>

                            <div class="ofi-color-grid">
                                <div class="ofi-color-card">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="ofi-color-swatch" style="background-color: #1f3a2c;"></div>
                                        <div>
                                            <div class="fw-semibold text-dark" style="font-size: 0.88rem;">Color principal</div>
                                            <div class="text-muted" style="font-size: 0.72rem;">Sidebar y botones primarios</div>
                                        </div>
                                    </div>
                                    <span class="text-muted font-monospace" style="font-size: 0.75rem;">#1F3A2C</span>
                                </div>

                                <div class="ofi-color-card">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="ofi-color-swatch" style="background-color: #2b4d3b;"></div>
                                        <div>
                                            <div class="fw-semibold text-dark" style="font-size: 0.88rem;">Color secundario</div>
                                            <div class="text-muted" style="font-size: 0.72rem;">Hover y elementos de apoyo</div>
                                        </div>
                                    </div>
                                    <span class="text-muted font-monospace" style="font-size: 0.75rem;">#2B4D3B</span>
                                </div>

                                <div class="ofi-color-card">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="ofi-color-swatch" style="background-color: #e8efe9;"></div>
                                        <div>
                                            <div class="fw-semibold text-dark" style="font-size: 0.88rem;">Color suave</div>
                                            <div class="text-muted" style="font-size: 0.72rem;">Fondos destacados y badges</div>
                                        </div>
                                    </div>
                                    <span class="text-muted font-monospace" style="font-size: 0.75rem;">#E8EFE9</span>
                                </div>

                                <div class="ofi-color-card">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="ofi-color-swatch" style="background-color: #c9a54a;"></div>
                                        <div>
                                            <div class="fw-semibold text-dark" style="font-size: 0.88rem;">Color de acento</div>
                                            <div class="text-muted" style="font-size: 0.72rem;">Detalles y señalización</div>
                                        </div>
                                    </div>
                                    <span class="text-muted font-monospace" style="font-size: 0.75rem;">#C9A54A</span>
                                </div>
                            </div>
                        </div>

                        <c:if test="${not empty usuarioPerfil}">
                            <div>
                                <div class="d-flex align-items-center gap-2 mb-1">
                                    <span class="ofi-step-pill">03</span>
                                    <h2 class="h5 fw-bold mb-0 text-dark">Perfil del Superadministrador</h2>
                                </div>
                                <p class="sij-texto-suave mb-3" style="font-size: 0.85rem; padding-left: 34px;">
                                    Actualiza el nombre mostrado en el sistema. Para contraseña usa "Cambiar contraseña".
                                </p>
                                <div class="sij-tarjeta p-3">
                                    <div class="row g-3">
                                        <div class="col-md-4">
                                            <label class="form-label fw-semibold" for="perfil-nombre">Nombre *</label>
                                            <input id="perfil-nombre" name="nombre" type="text" maxlength="50" required class="form-control"
                                                   value="<c:out value='${usuarioPerfil.nombre}'/>">
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label fw-semibold" for="perfil-paterno">Apellido paterno *</label>
                                            <input id="perfil-paterno" name="apellidoPaterno" type="text" maxlength="50" required class="form-control"
                                                   value="<c:out value='${usuarioPerfil.apellidoPaterno}'/>">
                                        </div>
                                        <div class="col-md-4">
                                            <label class="form-label fw-semibold" for="perfil-materno">Apellido materno</label>
                                            <input id="perfil-materno" name="apellidoMaterno" type="text" maxlength="50" class="form-control"
                                                   value="<c:out value='${usuarioPerfil.apellidoMaterno}'/>">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:if>
                    </div>

                    <div class="col-lg-5">
                        <div class="ofi-preview-container">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <div class="sij-subtitulo-dorado text-uppercase fw-bold" style="letter-spacing: .08em; font-size: 0.72rem;">VISTA PREVIA</div>
                                <span class="ofi-badge-live">&bull; En vivo</span>
                            </div>
                            <h3 class="h5 fw-bold mb-3 text-dark">Así se verá tu interfaz</h3>

                            <div class="ofi-mock-app">
                                <div class="ofi-mock-sidebar">
                                    <div class="ofi-circle-logo" style="width: 24px; height: 24px; font-size: 0.55rem; border-width: 1px;">PJ</div>
                                    <div style="height: 3px; width: 36px; background-color: var(--sij-acento); border-radius: 2px; margin-top: 0.25rem;"></div>
                                    <div style="height: 4px; width: 44px; background-color: rgba(255,255,255,0.25); border-radius: 2px;"></div>
                                    <div style="height: 4px; width: 40px; background-color: rgba(255,255,255,0.25); border-radius: 2px;"></div>
                                </div>
                                <div class="ofi-mock-main">
                                    <div style="color: var(--sij-acento); font-size: 0.58rem; font-weight: 700; letter-spacing: 0.08em;">RESUMEN GENERAL</div>
                                    <div class="fw-bold text-dark" style="font-size: 0.95rem; line-height: 1.1;">Bienvenido</div>
                                    <div class="text-muted" style="font-size: 0.65rem;">Panel de administración institucional</div>

                                    <div class="d-flex gap-2 my-2">
                                        <div style="flex: 1; height: 42px; background: #ffffff; border: 1px solid var(--sij-borde); border-radius: 4px;"></div>
                                        <div style="flex: 1; height: 42px; background: #ffffff; border: 1px solid var(--sij-borde); border-radius: 4px;"></div>
                                        <div style="flex: 1; height: 42px; background: #ffffff; border: 1px solid var(--sij-borde); border-radius: 4px;"></div>
                                    </div>

                                    <div>
                                        <button type="button" class="btn btn-primary" style="font-size: 0.68rem; padding: 0.25rem 0.65rem; border-radius: 4px;" disabled>
                                            Acción principal
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <div class="ofi-alert-info-box">
                                <span>Los colores están alineados con la plantilla del Sistema Judicial Integral y el módulo de Peritajes.</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="ofi-sticky-footer-bar">
                    <div class="text-muted small">
                        ¿Todo listo? Guarda los cambios para aplicarlos.
                    </div>
                    <div class="d-flex gap-2">
                        <a href="<c:url value='/oficialia'/>" class="btn btn-sij-secundario">
                            Cancelar cambios
                        </a>
                        <button type="submit" class="btn btn-primary">
                            Guardar personalización
                        </button>
                    </div>
                </div>
            </form>
        </main>

        <footer class="sij-pie text-center">
            Poder Judicial del Estado de Morelos · Sistema Judicial Integral &copy; 2026
        </footer>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<c:url value='/recursos/nucleo/js/comun.js?v=2'/>"></script>
<script src="<c:url value='/recursos/oficialia/js/main.js?v=2'/>"></script>
</body>
</html>
