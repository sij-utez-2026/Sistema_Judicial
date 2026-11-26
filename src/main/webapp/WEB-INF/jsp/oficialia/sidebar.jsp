<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="n" tagdir="/WEB-INF/tags/nucleo" %>
<c:set var="sec" value="${empty param.active ? 'inicio' : param.active}"/>
<aside class="sij-sidebar offcanvas-lg offcanvas-start" tabindex="-1" id="sijMenu" aria-label="Menú principal">
    <div class="offcanvas-body">
        <div class="d-flex justify-content-between align-items-center mb-3">
            <a class="ofi-sidebar-logo mb-0" href="<c:url value='/oficialia'/>">
                <div class="ofi-circle-logo">PJ</div>
                <div class="ofi-pj-text">
                    <div class="ofi-pj-title" style="font-size: 0.82rem;">Poder Judicial</div>
                    <div class="ofi-pj-sub" style="font-size: 0.62rem;">del Estado de Morelos</div>
                </div>
            </a>
            <button type="button" class="btn-close btn-close-white d-lg-none" data-bs-dismiss="offcanvas"
                    data-bs-target="#sijMenu" aria-label="Cerrar menú"></button>
        </div>

        <div class="sij-linea-dorada mt-0 mb-2"></div>

        <div class="sij-etiqueta-menu mt-2 mb-2">Espacio de trabajo</div>
        <nav class="nav flex-column sij-menu" aria-label="Menú principal">
            <a href="<c:url value='/oficialia'/>" class="nav-link${sec eq 'inicio' ? ' active' : ''}">
                <n:icono nombre="inicio" tamano="16"/>
                <span>Inicio</span>
            </a>
            <a href="<c:url value='/oficialia/distritos'/>" class="nav-link${sec eq 'distritos' ? ' active' : ''}">
                <n:icono nombre="edificio" tamano="16"/>
                <span>Distritos</span>
            </a>
            <a href="<c:url value='/oficialia/juzgados'/>" class="nav-link${sec eq 'juzgados' ? ' active' : ''}">
                <n:icono nombre="balanza" tamano="16"/>
                <span>Juzgados</span>
            </a>
            <a href="<c:url value='/oficialia/oficialias'/>" class="nav-link${sec eq 'oficialias' ? ' active' : ''}">
                <n:icono nombre="carpeta" tamano="16"/>
                <span>Oficialías de Partes</span>
            </a>
            <a href="<c:url value='/oficialia/materias'/>" class="nav-link${sec eq 'materias' ? ' active' : ''}">
                <n:icono nombre="documento" tamano="16"/>
                <span>Materias</span>
            </a>
            <a href="<c:url value='/oficialia/personalizacion'/>" class="nav-link${sec eq 'personalizacion' ? ' active' : ''}">
                <n:icono nombre="editar" tamano="16"/>
                <span>Personalización</span>
            </a>
        </nav>

        <div class="sij-etiqueta-menu">Sistema</div>
        <nav class="nav flex-column sij-menu" aria-label="Menú del sistema">
            <c:if test="${usuarioSesion.administradorGeneral}">
                <a href="<c:url value='/'/>" class="nav-link">
                    <n:icono nombre="inicio" tamano="16"/>
                    <span>Inicio del sistema</span>
                </a>
            </c:if>
            <a href="<c:url value='/cuenta/password'/>" class="nav-link">
                <n:icono nombre="persona" tamano="16"/>
                <span>Cambiar contraseña</span>
            </a>
        </nav>

        <div class="sij-usuario">
            <div class="ofi-user-badge">
                <div class="d-flex align-items-center gap-2 overflow-hidden">
                    <div class="sij-avatar" style="width: 34px; height: 34px; font-size: 0.82rem;">
                        <c:out value="${empty usuarioActual ? 'SA' : usuarioActual.iniciales}"/>
                    </div>
                    <div class="overflow-hidden">
                        <div class="fw-semibold text-truncate text-white" style="font-size: 0.82rem;">
                            <c:out value="${empty usuarioActual ? 'Administrador' : usuarioActual.nombre}"/>
                        </div>
                        <div class="text-truncate sij-subtitulo-dorado" style="font-size: 0.7rem;">
                            <c:out value="${empty usuarioActual ? 'Administrador general' : usuarioActual.rol}"/>
                        </div>
                    </div>
                </div>
            </div>

            <form method="post" action="<c:url value='/logout'/>">
                <n:csrf/>
                <button type="submit" class="btn sij-btn-salir w-100 text-start">
                    &#8614; Cerrar sesión
                </button>
            </form>
        </div>
    </div>
</aside>
