<%@ tag description="Plantilla de inicio de sesión y recuperación (INTEGRADORES)" pageEncoding="UTF-8" %>
<%--
  Pantallas sin sesión del diseño "Acceso institucional" y "Recuperación de contraseña" (docs/modulos/img.png):
  a la izquierda el panel verde con el emblema y PJEM; a la derecha el formulario.

    <t:layout-acceso titulo="Iniciar sesión">
        ... formulario ...
    </t:layout-acceso>
--%>
<%@ attribute name="titulo" required="true" %>
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
    <title><c:out value="${titulo}"/> · Sistema Judicial Integral</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <%-- ?v= : se sube al cambiar el archivo para que el navegador no use el viejo guardado en caché --%>
    <link rel="stylesheet" href="<c:url value='/recursos/nucleo/css/estilos.css?v=2'/>">
    <link rel="stylesheet" href="<c:url value='/recursos/nucleo/css/acceso.css?v=1'/>">
</head>
<body class="sij-body">
<div class="sij-acceso">
    <%-- Panel institucional --%>
    <aside class="sij-acceso-panel">
        <div class="sij-acceso-centro">
            <div class="sij-acceso-emblema">Reservado para<br>emblema oficial</div>
            <div class="sij-acceso-siglas">PJEM</div>
            <div class="sij-acceso-linea"></div>
            <div class="sij-acceso-institucion">Poder Judicial del Estado de Morelos</div>
        </div>
        <div class="sij-acceso-pie">
            <div>Acceso exclusivo para personal autorizado</div>
            <div>Poder Judicial · Morelos</div>
        </div>
    </aside>

    <%-- Formulario --%>
    <main class="sij-acceso-formulario" id="contenido">
        <div class="sij-acceso-caja">
            <n:mensajes/>
            <jsp:doBody/>
        </div>
    </main>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<c:url value='/recursos/nucleo/js/comun.js?v=2'/>"></script>
</body>
</html>
