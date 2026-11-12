<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.edu.utez.oficialiaparte.model.Usuario" %>
<%
    String active = request.getParameter("active");
    if (active == null) active = "inicio";
    Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
    String nombreUsuario = (usuarioActual != null) ? usuarioActual.getNombre() : "Elena Martínez Salgado";
    String rolUsuario = "Administrador del sistema";
    String inicialesUsuario = (usuarioActual != null && usuarioActual.getIniciales() != null) ? usuarioActual.getIniciales() : "EM";
%>
<aside class="sij-sidebar offcanvas-lg offcanvas-start" tabindex="-1" id="sijMenu" aria-label="Menú principal">
    <div class="offcanvas-body">
        <!-- Logo Institucional PJ Circular -->
        <div class="d-flex justify-content-between align-items-center mb-3">
            <a class="ofi-sidebar-logo mb-0" href="${pageContext.request.contextPath}/inicio">
                <div class="ofi-circle-logo">PJ</div>
                <div class="ofi-pj-text">
                    <div class="ofi-pj-title" style="font-size: 0.82rem;">Poder Judicial</div>
                    <div class="ofi-pj-sub" style="font-size: 0.62rem;">del Estado de Morelos</div>
                </div>
            </a>
            <button type="button" class="btn-close btn-close-white d-lg-none" data-bs-dismiss="offcanvas"
                    data-bs-target="#sijMenu" aria-label="Cerrar menú"></button>
        </div>

        <div class="sij-etiqueta-menu mt-2 mb-2" style="color: rgba(255, 255, 255, 0.45); font-size: 0.68rem; font-weight: 700; letter-spacing: 0.1em; text-transform: uppercase;">
            Espacio de trabajo
        </div>

        <!-- Navegación según nuevas capturas -->
        <nav class="nav flex-column sij-menu" aria-label="Menú principal">
            <!-- 1. Inicio -->
            <a href="${pageContext.request.contextPath}/inicio" class="nav-link <%= "inicio".equals(active) ? "active" : "" %>">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="m3 9 9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                    <polyline points="9 22 9 12 15 12 15 22"></polyline>
                </svg>
                <span>Inicio</span>
            </a>

            <!-- 2. Distritos -->
            <a href="${pageContext.request.contextPath}/distritos" class="nav-link <%= "distritos".equals(active) ? "active" : "" %>">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polygon points="1 6 1 22 8 18 16 22 23 18 23 2 16 6 8 2 1 6"></polygon>
                    <line x1="8" y1="2" x2="8" y2="18"></line>
                    <line x1="16" y1="6" x2="16" y2="22"></line>
                </svg>
                <span>Distritos</span>
            </a>

            <!-- 3. Juzgados -->
            <a href="${pageContext.request.contextPath}/juzgados" class="nav-link <%= "juzgados".equals(active) ? "active" : "" %>">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 21h18"></path>
                    <path d="M5 21V9"></path>
                    <path d="M19 21V9"></path>
                    <path d="M9 21V9"></path>
                    <path d="M15 21V9"></path>
                    <path d="m2 9 10-6 10 6"></path>
                </svg>
                <span>Juzgados</span>
            </a>

            <!-- 4. Oficialías de Partes -->
            <a href="${pageContext.request.contextPath}/oficialias" class="nav-link <%= "oficialias".equals(active) ? "active" : "" %>">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="22 12 16 12 14 15 10 15 8 12 2 12"></polyline>
                    <path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11z"></path>
                </svg>
                <span>Oficialías de Partes</span>
            </a>

            <!-- 5. Materias -->
            <a href="${pageContext.request.contextPath}/materias" class="nav-link <%= "materias".equals(active) ? "active" : "" %>">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                </svg>
                <span>Materias</span>
            </a>

            <!-- 6. Personalización -->
            <a href="${pageContext.request.contextPath}/personalizacion" class="nav-link <%= "personalizacion".equals(active) ? "active" : "" %>">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="13.5" cy="6.5" r=".5" fill="currentColor"></circle>
                    <circle cx="17.5" cy="10.5" r=".5" fill="currentColor"></circle>
                    <circle cx="8.5" cy="7.5" r=".5" fill="currentColor"></circle>
                    <circle cx="6.5" cy="12.5" r=".5" fill="currentColor"></circle>
                    <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z"></path>
                </svg>
                <span>Personalización</span>
            </a>
        </nav>

        <!-- Bloque de Usuario (Pie del Menú) -->
        <div class="sij-usuario">
            <div class="ofi-user-badge">
                <div class="d-flex align-items-center gap-2 overflow-hidden">
                    <div class="sij-avatar" style="background: #f7f3e6; color: var(--sij-primario); width: 34px; height: 34px; font-size: 0.82rem; font-weight: 700;">
                        <%= inicialesUsuario %>
                    </div>
                    <div class="overflow-hidden">
                        <div class="fw-semibold text-truncate text-white" style="font-size: 0.82rem;"><%= nombreUsuario %></div>
                        <div class="text-truncate" style="color: rgba(255,255,255,0.55); font-size: 0.7rem;"><%= rolUsuario %></div>
                    </div>
                </div>
                <div class="text-white-50" style="font-size: 0.8rem;">&rsaquo;</div>
            </div>

            <a href="${pageContext.request.contextPath}/logout" class="d-flex align-items-center gap-2 text-white-50 text-decoration-none px-2 py-1" style="font-size: 0.82rem;">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                    <polyline points="16 17 21 12 16 7"></polyline>
                    <line x1="21" y1="12" x2="9" y2="12"></line>
                </svg>
                <span>Cerrar sesión</span>
            </a>
        </div>
    </div>
</aside>
