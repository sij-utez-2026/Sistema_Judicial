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
    <title>Panel de Administración · Oficialía de Partes</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="<c:url value='/recursos/nucleo/css/estilos.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/recursos/oficialia/css/oficialia.css?v=2'/>">
</head>
<body class="sij-body">
<div class="sij-app">
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="inicio"/>
    </jsp:include>

    <div class="sij-contenido">
        <jsp:include page="topbar.jsp"/>

        <main class="sij-main" id="contenido">
            <n:mensajes/>

            <div class="mb-4">
                <div class="ofi-eyebrow mb-1">RESUMEN GENERAL</div>
                <h1 class="sij-titulo mt-0 mb-1" style="font-size: 2rem; font-weight: 600;">
                    Bienvenido(a), <c:out value="${empty usuarioActual ? 'Administrador' : usuarioActual.nombre}"/>
                </h1>
                <p class="sij-descripcion" style="font-size: 0.95rem;">
                    Consulta y administra la estructura institucional del Poder Judicial de Morelos.
                </p>
            </div>

            <div class="ofi-grid-dashboard">
                <a href="<c:url value='/oficialia/distritos'/>" class="ofi-card-photo ofi-bg-distritos">
                    <div>
                        <div class="ofi-photo-number"><c:out value="${totalDistritos}"/></div>
                        <div class="ofi-photo-label">Distritos registrados</div>
                    </div>
                    <div>
                        <div class="ofi-photo-divider"></div>
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="ofi-photo-link">Ver detalle</span>
                            <span class="text-white-50">&#8594;</span>
                        </div>
                    </div>
                </a>

                <a href="<c:url value='/oficialia/juzgados'/>" class="ofi-card-photo ofi-bg-juzgados">
                    <div>
                        <div class="ofi-photo-number"><c:out value="${totalJuzgados}"/></div>
                        <div class="ofi-photo-label">Juzgados registrados</div>
                    </div>
                    <div>
                        <div class="ofi-photo-divider"></div>
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="ofi-photo-link">Ver detalle</span>
                            <span class="text-white-50">&#8594;</span>
                        </div>
                    </div>
                </a>

                <a href="<c:url value='/oficialia/oficialias'/>" class="ofi-card-photo ofi-bg-oficialias">
                    <div>
                        <div class="ofi-photo-number"><c:out value="${totalOficialias}"/></div>
                        <div class="ofi-photo-label">Oficialías registradas</div>
                    </div>
                    <div>
                        <div class="ofi-photo-divider"></div>
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="ofi-photo-link">Ver detalle</span>
                            <span class="text-white-50">&#8594;</span>
                        </div>
                    </div>
                </a>

                <a href="<c:url value='/oficialia/materias'/>" class="ofi-card-photo ofi-bg-materias">
                    <div>
                        <div class="ofi-photo-number"><c:out value="${totalMaterias}"/></div>
                        <div class="ofi-photo-label">Materias registradas</div>
                    </div>
                    <div>
                        <div class="ofi-photo-divider"></div>
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="ofi-photo-link">Ver detalle</span>
                            <span class="text-white-50">&#8594;</span>
                        </div>
                    </div>
                </a>
            </div>
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
