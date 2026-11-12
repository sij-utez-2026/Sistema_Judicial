<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.edu.utez.oficialiaparte.model.Usuario" %>
<%
    Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
    if (usuarioActual == null) {
        usuarioActual = new Usuario(1, "Elena Martínez Salgado", "elena.martinez@tsjmorelos.gob.mx", "admin123", "Administrador del sistema", "EM");
    }
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Personalización · Sistema de Oficialía de Partes</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/recursos/nucleo/css/estilos.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/recursos/oficialia/css/oficialia.css">
</head>
<body class="sij-body">
<div class="sij-app">

    <!-- Barra lateral -->
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="personalizacion"/>
    </jsp:include>

    <!-- Contenido Principal -->
    <div class="sij-contenido">
        <jsp:include page="topbar.jsp"/>

        <main class="sij-main" id="contenido">
            <!-- Encabezado de Sección (Captura 7) -->
            <div class="mb-4">
                <div class="sij-etiqueta-menu mt-0 mb-1" style="color: var(--sij-acento); font-size: 0.72rem; font-weight: 700; letter-spacing: 0.12em;">IDENTIDAD INSTITUCIONAL</div>
                <h1 class="sij-titulo mt-0 mb-1" style="font-size: 2.1rem; font-weight: 600;">Personalización</h1>
                <p class="sij-descripcion" style="font-size: 0.95rem;">Configura el logotipo y los colores de la interfaz institucional.</p>
            </div>

            <% if ("ok".equals(msg)) { %>
            <div class="sij-aviso p-3 mb-3 border-success text-success">
                &#10004; Configuración de personalización guardada y aplicada exitosamente.
            </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/personalizacion" method="POST">
                <div class="row g-4">
                    <!-- Columna Izquierda: Configuración (Sección 01 y 02) -->
                    <div class="col-lg-7">
                        <!-- 01 Logotipo institucional -->
                        <div class="mb-5">
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
                                    </div>
                                </div>
                                <div>
                                    <button type="button" class="btn btn-sij-secundario shadow-sm d-flex align-items-center gap-2" onclick="document.getElementById('input-logo-file').click()">
                                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
                                            <polyline points="17 8 12 3 7 8"></polyline>
                                            <line x1="12" y1="3" x2="12" y2="15"></line>
                                        </svg>
                                        <span>Cambiar logotipo</span>
                                    </button>
                                    <input type="file" id="input-logo-file" style="display: none;" accept="image/png, image/svg+xml">
                                </div>
                            </div>
                        </div>

                        <!-- 02 Colores de la interfaz -->
                        <div>
                            <div class="d-flex align-items-center gap-2 mb-1">
                                <span class="ofi-step-pill">02</span>
                                <h2 class="h5 fw-bold mb-0 text-dark">Colores de la interfaz</h2>
                            </div>
                            <p class="sij-texto-suave mb-3" style="font-size: 0.85rem; padding-left: 34px;">
                                Selecciona los tonos principales del sistema.
                            </p>

                            <div class="ofi-color-grid">
                                <!-- Tarjeta 1: Color principal -->
                                <div class="ofi-color-card">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="ofi-color-swatch" style="background-color: #2E3B2B;"></div>
                                        <div>
                                            <div class="fw-semibold text-dark" style="font-size: 0.88rem;">Color principal</div>
                                            <div class="text-muted" style="font-size: 0.72rem;">Sidebar y navegación</div>
                                        </div>
                                    </div>
                                    <span class="text-muted font-monospace" style="font-size: 0.75rem;">#2E3B2B</span>
                                </div>

                                <!-- Tarjeta 2: Color secundario -->
                                <div class="ofi-color-card">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="ofi-color-swatch" style="background-color: #2E3B2B;"></div>
                                        <div>
                                            <div class="fw-semibold text-dark" style="font-size: 0.88rem;">Color secundario</div>
                                            <div class="text-muted" style="font-size: 0.72rem;">Títulos y elementos de apoyo</div>
                                        </div>
                                    </div>
                                    <span class="text-muted font-monospace" style="font-size: 0.75rem;">#2E3B2B</span>
                                </div>

                                <!-- Tarjeta 3: Color de acción -->
                                <div class="ofi-color-card">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="ofi-color-swatch" style="background-color: #773357;"></div>
                                        <div>
                                            <div class="fw-semibold text-dark" style="font-size: 0.88rem;">Color de acción</div>
                                            <div class="text-muted" style="font-size: 0.72rem;">Botones y acciones destacadas</div>
                                        </div>
                                    </div>
                                    <span class="text-muted font-monospace" style="font-size: 0.75rem;">#773357</span>
                                </div>

                                <!-- Tarjeta 4: Color de acento -->
                                <div class="ofi-color-card">
                                    <div class="d-flex align-items-center gap-3">
                                        <div class="ofi-color-swatch" style="background-color: #C49A6D;"></div>
                                        <div>
                                            <div class="fw-semibold text-dark" style="font-size: 0.88rem;">Color de acento</div>
                                            <div class="text-muted" style="font-size: 0.72rem;">Detalles y señalización</div>
                                        </div>
                                    </div>
                                    <span class="text-muted font-monospace" style="font-size: 0.75rem;">#C49A6D</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Columna Derecha: Vista Previa en Vivo (Captura 7) -->
                    <div class="col-lg-5">
                        <div class="ofi-preview-container">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <div class="sij-subtitulo-dorado text-uppercase fw-bold" style="letter-spacing: .08em; font-size: 0.72rem;">VISTA PREVIA</div>
                                <span class="ofi-badge-live">&bull; En vivo</span>
                            </div>
                            <h3 class="h5 fw-bold mb-3 text-dark">Así se verá tu interfaz</h3>

                            <!-- Mockup Miniatura de la Interfaz -->
                            <div class="ofi-mock-app">
                                <div class="ofi-mock-sidebar">
                                    <div class="ofi-circle-logo" style="width: 24px; height: 24px; font-size: 0.55rem; border-width: 1px;">PJ</div>
                                    <div style="height: 3px; width: 36px; background-color: var(--ofi-color-acento); border-radius: 2px; margin-top: 0.25rem;"></div>
                                    <div style="height: 4px; width: 44px; background-color: rgba(255,255,255,0.2); border-radius: 2px;"></div>
                                    <div style="height: 4px; width: 40px; background-color: rgba(255,255,255,0.2); border-radius: 2px;"></div>
                                </div>
                                <div class="ofi-mock-main">
                                    <div style="color: var(--ofi-color-acento); font-size: 0.58rem; font-weight: 700; letter-spacing: 0.08em;">RESUMEN GENERAL</div>
                                    <div class="fw-bold text-dark" style="font-size: 0.95rem; line-height: 1.1;">Bienvenido</div>
                                    <div class="text-muted" style="font-size: 0.65rem;">Panel de administración institucional</div>

                                    <div class="d-flex gap-2 my-2">
                                        <div style="flex: 1; height: 42px; background: #ffffff; border: 1px solid #e3dcce; border-radius: 4px;"></div>
                                        <div style="flex: 1; height: 42px; background: #ffffff; border: 1px solid #e3dcce; border-radius: 4px;"></div>
                                        <div style="flex: 1; height: 42px; background: #ffffff; border: 1px solid #e3dcce; border-radius: 4px;"></div>
                                    </div>

                                    <div>
                                        <button type="button" class="btn btn-primary" style="font-size: 0.68rem; padding: 0.25rem 0.65rem; border-radius: 4px;" disabled>
                                            Acción principal
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <!-- Nota informativa -->
                            <div class="ofi-alert-info-box">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink: 0;">
                                    <circle cx="12" cy="12" r="10"></circle>
                                    <line x1="12" y1="16" x2="12" y2="12"></line>
                                    <line x1="12" y1="8" x2="12.01" y2="8"></line>
                                </svg>
                                <span>Los cambios se aplicarán a toda la interfaz después de guardarlos.</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Barra Inferior de Guardado (Captura 10) -->
                <div class="ofi-sticky-footer-bar">
                    <div class="text-muted fs-7">
                        ¿Todo listo? Guarda los cambios para aplicarlos.
                    </div>
                    <div class="d-flex gap-2">
                        <a href="${pageContext.request.contextPath}/inicio" class="btn btn-sij-secundario">
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

<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>
