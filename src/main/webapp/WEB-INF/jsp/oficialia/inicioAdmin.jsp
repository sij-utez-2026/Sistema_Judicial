<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="mx.edu.utez.oficialiaparte.model.Usuario" %>
<%
    Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
    String primerNombre = "Elena";
    if (usuarioActual != null && usuarioActual.getNombre() != null) {
        primerNombre = usuarioActual.getNombre().split("\\s+")[0];
    }
    int totalDistritos = request.getAttribute("totalDistritos") != null ? (Integer) request.getAttribute("totalDistritos") : 4;
    int totalJuzgados = request.getAttribute("totalJuzgados") != null ? (Integer) request.getAttribute("totalJuzgados") : 5;
    int totalOficialias = request.getAttribute("totalOficialias") != null ? (Integer) request.getAttribute("totalOficialias") : 3;
    int totalMaterias = request.getAttribute("totalMaterias") != null ? (Integer) request.getAttribute("totalMaterias") : 5;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Panel de Administración · Sistema de Oficialía de Partes</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/recursos/nucleo/css/estilos.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/recursos/oficialia/css/oficialia.css">
</head>
<body class="sij-body">
<div class="sij-app">

    <!-- Barra lateral -->
    <jsp:include page="sidebar.jsp">
        <jsp:param name="active" value="inicio"/>
    </jsp:include>

    <!-- Contenido Principal -->
    <div class="sij-contenido">
        <jsp:include page="topbar.jsp"/>

        <main class="sij-main" id="contenido">
            <div class="mb-4">
                <div class="sij-etiqueta-menu mt-0 mb-1" style="color: var(--sij-acento); font-size: 0.72rem; font-weight: 700; letter-spacing: 0.12em;">RESUMEN GENERAL</div>
                <h1 class="sij-titulo mt-0 mb-1" style="font-size: 2.1rem; font-weight: 600;">Bienvenida, <%= primerNombre %></h1>
                <p class="sij-descripcion" style="font-size: 0.95rem;">Consulta y administra la estructura institucional del Poder Judicial de Morelos.</p>
            </div>

            <!-- Grid 2x2 de Tarjetas Fotográficas según Boceto 1 -->
            <div class="ofi-grid-dashboard">
                <!-- Tarjeta 1: Distritos -->
                <a href="${pageContext.request.contextPath}/distritos" class="ofi-card-photo ofi-bg-distritos">
                    <div>
                        <div class="ofi-photo-number"><%= String.format("%02d", totalDistritos) %></div>
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

                <!-- Tarjeta 2: Juzgados -->
                <a href="${pageContext.request.contextPath}/juzgados" class="ofi-card-photo ofi-bg-juzgados">
                    <div>
                        <div class="ofi-photo-number"><%= String.format("%02d", totalJuzgados) %></div>
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

                <!-- Tarjeta 3: Oficialías -->
                <a href="${pageContext.request.contextPath}/oficialias" class="ofi-card-photo ofi-bg-oficialias">
                    <div>
                        <div class="ofi-photo-number"><%= String.format("%02d", totalOficialias) %></div>
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

                <!-- Tarjeta 4: Materias -->
                <a href="${pageContext.request.contextPath}/materias" class="ofi-card-photo ofi-bg-materias">
                    <div>
                        <div class="ofi-photo-number"><%= String.format("%02d", totalMaterias) %></div>
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

<script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
</body>
</html>
